package net.primal.android.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.isValidHex
import net.primal.android.navigation.primalName
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.details.ProfileDetailsContract.UiEvent
import net.primal.android.profile.details.ProfileDetailsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.FEED_KIND_USER
import net.primal.domain.FeedSpecKind
import net.primal.domain.buildLatestNotesUserFeedSpec
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.repository.FeedsRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository
import timber.log.Timber

@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<ProfileDetailsContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: ProfileDetailsContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    private var referencedProfilesObserver: Job? = null

    init {
        observeEvents()
        observeActiveAccount()
        resolveProfileId()
    }

    private fun resolveProfileId() =
        viewModelScope.launch {
            setState { copy(isResolvingProfileId = true) }
            runCatching {
                val profileId = withContext(dispatcherProvider.io()) {
                    savedStateHandle.profileId?.parseForProfileId()
                        ?: savedStateHandle.primalName?.let { profileRepository.fetchProfileId(it) }
                }

                if (profileId?.isValidHex() == true) {
                    val isActiveUser = profileId == activeAccountStore.activeUserId()
                    setState { copy(profileId = profileId, isActiveUser = isActiveUser) }
                    initializeProfileDetails(profileId = profileId, isActiveUser = isActiveUser)
                }
            }
            setState { copy(isResolvingProfileId = false) }
        }

    private fun initializeProfileDetails(profileId: String, isActiveUser: Boolean) {
        requestProfileUpdate(profileId = profileId)
        observeProfileData(profileId = profileId)
        observeIsProfileFollowed(profileId = profileId)
        observeReferencedProfilesData(profileId = profileId)
        observeProfileStats(profileId = profileId)
        observeContainsFeed(profileId = profileId)
        observeMutedAccount(profileId = profileId)
        resolveFollowsMe(profileId = profileId)
        markProfileInteraction(profileId = profileId, isActiveUser = isActiveUser)
    }

    private fun markProfileInteraction(isActiveUser: Boolean, profileId: String) {
        if (!isActiveUser) {
            viewModelScope.launch {
                userRepository.markAsInteracted(
                    profileId = profileId,
                    ownerId = activeAccountStore.activeUserId(),
                )
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.FollowAction -> follow(event)
                    is UiEvent.UnfollowAction -> unfollow(event)
                    is UiEvent.AddProfileFeedAction -> addProfileFeed(event)
                    is UiEvent.RemoveProfileFeedAction -> removeProfileFeed(event)
                    is UiEvent.MuteAction -> mute(event)
                    is UiEvent.UnmuteAction -> unmute(event)
                    is UiEvent.ReportAbuse -> reportAbuse(event)
                    UiEvent.RequestProfileUpdate -> _state.value.profileId?.let { requestProfileUpdate(it) }
                    UiEvent.RequestProfileIdResolution -> resolveProfileId()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.DismissZapError -> setState { copy(zapError = null) }

                    is UiEvent.ZapProfile -> zapProfile(
                        profileId = event.profileId,
                        profileLnUrlDecoded = event.profileLnUrlDecoded,
                        zapAmount = event.zapAmount,
                        zapDescription = event.zapDescription,
                    )

                    UiEvent.DismissConfirmFollowUnfollowAlertDialog ->
                        setState { copy(shouldApproveProfileAction = null) }
                }
            }
        }

    private fun zapProfile(
        profileId: String,
        profileLnUrlDecoded: String?,
        zapAmount: ULong?,
        zapDescription: String?,
    ) = viewModelScope.launch {
        if (profileLnUrlDecoded == null) {
            setState { copy(zapError = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
            return@launch
        }

        try {
            zapHandler.zap(
                userId = activeAccountStore.activeUserId(),
                target = ZapTarget.Profile(
                    profileId = profileId,
                    profileLnUrlDecoded = profileLnUrlDecoded,
                ),
                amountInSats = zapAmount,
                comment = zapDescription,
            )
            setEffect(ProfileDetailsContract.SideEffect.ProfileZapSent)
        } catch (error: ZapFailureException) {
            setState { copy(zapError = UiError.FailedToPublishZapEvent(error)) }
            Timber.w(error)
        } catch (error: MissingRelaysException) {
            setState { copy(zapError = UiError.MissingRelaysConfiguration(error)) }
            Timber.w(error)
        } catch (error: InvalidZapRequestException) {
            setState { copy(zapError = UiError.InvalidZapRequest(error)) }
            Timber.w(error)
        }
    }

    private fun requestProfileUpdate(profileId: String) =
        viewModelScope.launch {
            fetchLatestProfile(profileId = profileId)
            fetchProfileFollowedBy(profileId = profileId)
            fetchLatestMuteList()
            setEffect(ProfileDetailsContract.SideEffect.ProfileUpdateFinished)
        }

    private fun fetchProfileFollowedBy(profileId: String) =
        viewModelScope.launch {
            try {
                val profiles = profileRepository.fetchUserProfileFollowedBy(
                    profileId = profileId,
                    userId = activeAccountStore.activeUserId(),
                    limit = 10,
                )
                setState {
                    copy(
                        userFollowedByProfiles = profiles.map {
                            it.asProfileDetailsUi()
                        }.filterNot {
                            it == state.value.profileDetails
                        }.sortedByDescending { it.premiumDetails?.tier?.isPrimalLegendTier() == true },
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeUserPremiumTier = it.premiumMembership?.tier,
                        zappingState = this.zappingState.copy(
                            walletConnected = it.hasWallet(),
                            walletPreference = it.walletPreference,
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            walletBalanceInBtc = it.primalWalletState.balanceInBtc,
                        ),
                    )
                }
            }
        }

    private fun observeIsProfileFollowed(profileId: String) =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        isProfileFollowed = it.following.contains(profileId),
                    )
                }
            }
        }

    private fun observeContainsFeed(profileId: String) =
        viewModelScope.launch {
            val feedSpec = buildLatestNotesUserFeedSpec(userId = profileId)
            feedsRepository.observeContainsFeedSpec(
                userId = activeAccountStore.activeUserId(),
                feedSpec = feedSpec,
            )
                .collect {
                    setState { copy(isProfileFeedInActiveUserFeeds = it) }
                }
        }

    private fun observeMutedAccount(profileId: String) =
        viewModelScope.launch {
            mutedUserRepository.observeIsUserMutedByOwnerId(
                pubkey = profileId,
                ownerId = activeAccountStore.activeUserId(),
            ).collect {
                setState { copy(isProfileMuted = it) }
            }
        }

    private fun observeProfileData(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId)
                .distinctUntilChanged()
                .collect { profileData ->
                    setState { copy(profileDetails = profileData.asProfileDetailsUi()) }
                }
        }

    private fun observeReferencedProfilesData(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId)
                .mapNotNull { profile ->
                    profile.aboutUris
                        .mapNotNull { it.extractProfileId() }
                        .filter { it != profileId }
                }
                .distinctUntilChanged()
                .collect { profileIds ->
                    launchReferencedProfilesObserver(profileIds = profileIds)
                    requestProfileUpdates(profileIds = profileIds)
                }
        }

    private suspend fun requestProfileUpdates(profileIds: List<String>) {
        profileIds.forEach { profileId ->
            try {
                profileRepository.fetchProfile(profileId = profileId)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
    }

    private fun launchReferencedProfilesObserver(profileIds: List<String>) {
        referencedProfilesObserver?.cancel()
        referencedProfilesObserver = viewModelScope.launch {
            profileRepository.observeProfileData(profileIds = profileIds).collect { profilesData ->
                setState { copy(referencedProfilesData = profilesData.map { it.asProfileDetailsUi() }.toSet()) }
            }
        }
    }

    private fun observeProfileStats(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileStats(profileId = profileId).collect {
                setState {
                    copy(profileStats = it?.asProfileStatsUi())
                }
            }
        }

    private fun resolveFollowsMe(profileId: String) {
        val activeUserId = activeAccountStore.activeUserId()
        if (profileId != activeUserId) {
            viewModelScope.launch {
                try {
                    val isFollowing = withContext(dispatcherProvider.io()) {
                        profileRepository.isUserFollowing(userId = activeUserId, targetUserId = profileId)
                    }
                    setState { copy(isProfileFollowingMe = isFollowing) }
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }
    }

    private suspend fun fetchLatestMuteList() =
        try {
            withContext(dispatcherProvider.io()) {
                mutedUserRepository.fetchAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                )
            }
        } catch (error: WssException) {
            Timber.w(error)
        }

    private suspend fun fetchLatestProfile(profileId: String) =
        try {
            profileRepository.fetchProfile(profileId = profileId)
        } catch (error: WssException) {
            Timber.w(error)
        }

    private fun updateStateProfileAsFollowedAndClearApprovalFlag() =
        setState {
            copy(isProfileFollowed = true, shouldApproveProfileAction = null)
        }

    private fun updateStateProfileAsUnfollowedAndClearApprovalFlag() =
        setState {
            copy(isProfileFollowed = false, shouldApproveProfileAction = null)
        }

    private fun follow(followAction: UiEvent.FollowAction) =
        viewModelScope.launch {
            updateStateProfileAsFollowedAndClearApprovalFlag()
            try {
                userRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = followAction.profileId,
                    forceUpdate = followAction.forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = UiError.FailedToFollowUser(error))
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = UiError.FailedToFollowUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = UiError.MissingRelaysConfiguration(error))
            } catch (error: UserRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setState {
                    copy(shouldApproveProfileAction = ProfileApproval.Follow(profileId = followAction.profileId))
                }
            }
        }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) =
        viewModelScope.launch {
            updateStateProfileAsUnfollowedAndClearApprovalFlag()
            try {
                userRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = unfollowAction.profileId,
                    forceUpdate = unfollowAction.forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = UiError.FailedToUnfollowUser(error))
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = UiError.FailedToUnfollowUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = UiError.MissingRelaysConfiguration(error))
            } catch (error: UserRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setState {
                    copy(
                        shouldApproveProfileAction = ProfileApproval.Unfollow(profileId = unfollowAction.profileId),
                    )
                }
            }
        }

    private fun addProfileFeed(action: UiEvent.AddProfileFeedAction) {
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                feedsRepository.addFeedLocally(
                    userId = userId,
                    feedSpec = buildLatestNotesUserFeedSpec(userId = action.profileId),
                    title = action.feedTitle,
                    description = action.feedDescription,
                    feedSpecKind = FeedSpecKind.Notes,
                    feedKind = FEED_KIND_USER,
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
                setEffect(ProfileDetailsContract.SideEffect.ProfileFeedAdded)
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToAddToFeed(error))
            }
        }
    }

    private fun removeProfileFeed(action: UiEvent.RemoveProfileFeedAction) {
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                feedsRepository.removeFeedLocally(
                    userId = userId,
                    feedSpec = buildLatestNotesUserFeedSpec(userId = action.profileId),
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
                setEffect(ProfileDetailsContract.SideEffect.ProfileFeedRemoved)
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToRemoveFeed(error))
            }
        }
    }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.muteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        mutedUserId = action.profileId,
                    )
                }
                setState { copy(isProfileMuted = true) }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToMuteUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingRelaysConfiguration(error))
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToMuteUser(error))
            }
        }

    private fun unmute(action: UiEvent.UnmuteAction) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.unmuteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        unmutedUserId = action.profileId,
                    )
                }
                setState { copy(isProfileMuted = false) }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToUnmuteUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingRelaysConfiguration(error))
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiError.FailedToUnmuteUser(error))
            }
        }

    private fun reportAbuse(event: UiEvent.ReportAbuse) =
        viewModelScope.launch {
            try {
                profileRepository.reportAbuse(
                    userId = activeAccountStore.activeUserId(),
                    reportType = event.type,
                    profileId = event.profileId,
                    eventId = event.noteId,
                )
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingPrivateKey)
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setErrorState(error = UiError.NostrSignUnauthorized)
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun setErrorState(error: UiError) {
        setState { copy(error = error) }
    }

    private fun String.parseForProfileId(): String? =
        when {
            this.startsWith("npub") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
            this.startsWith("nprofile1") -> {
                val pubkey = Nip19TLV.parseUriAsNprofileOrNull(this)?.pubkey
                runCatching { pubkey?.bech32ToHexOrThrow() }.getOrNull()
            }

            else -> this
        }
}
