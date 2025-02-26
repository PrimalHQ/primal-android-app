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
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.errors.UiError
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.feeds.domain.FEED_KIND_USER
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildLatestNotesUserFeedSpec
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.details.ProfileDetailsContract.UiEvent
import net.primal.android.profile.details.ProfileDetailsContract.UiState
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
    private val profileRepository: ProfileRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId?.resolveProfileId() ?: activeAccountStore.activeUserId()

    private val isActiveUser = profileId == activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        UiState(
            profileId = profileId,
            isActiveUser = isActiveUser,
        ),
    )
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
        observeProfileData()
        observeReferencedProfilesData()
        observeProfileStats()
        fetchProfileFollowedBy()
        observeActiveAccount()
        observeContainsFeed()
        observeMutedAccount()
        resolveFollowsMe()
        markProfileInteraction()
    }

    private fun markProfileInteraction() {
        if (!isActiveUser) {
            viewModelScope.launch {
                profileRepository.markAsInteracted(
                    profileId = profileId,
                    ownerId = activeAccountStore.activeUserId(),
                )
            }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowAction -> follow(it)
                    is UiEvent.UnfollowAction -> unfollow(it)
                    is UiEvent.AddProfileFeedAction -> addProfileFeed(it)
                    is UiEvent.RemoveProfileFeedAction -> removeProfileFeed(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.UnmuteAction -> unmute(it)
                    UiEvent.RequestProfileUpdate -> requestProfileUpdate()

                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.ZapProfile -> zapProfile(
                        profileId = it.profileId,
                        profileLnUrlDecoded = it.profileLnUrlDecoded,
                        zapAmount = it.zapAmount,
                        zapDescription = it.zapDescription,
                    )

                    UiEvent.DismissZapError -> setState { copy(zapError = null) }
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

    private fun requestProfileUpdate() =
        viewModelScope.launch {
            fetchLatestProfile()
            fetchLatestMuteList()
            setEffect(ProfileDetailsContract.SideEffect.ProfileUpdateFinished)
        }

    private fun fetchProfileFollowedBy() =
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
                        isProfileFollowed = it.following.contains(profileId),
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

    private fun observeContainsFeed() =
        viewModelScope.launch {
            val feedSpec = buildLatestNotesUserFeedSpec(userId = profileId)
            feedsRepository.observeContainsFeedSpec(userId = activeAccountStore.activeUserId(), feedSpec = feedSpec)
                .collect {
                    setState { copy(isProfileFeedInActiveUserFeeds = it) }
                }
        }

    private fun observeMutedAccount() =
        viewModelScope.launch {
            mutedUserRepository.observeIsUserMutedByOwnerId(
                pubkey = profileId,
                ownerId = activeAccountStore.activeUserId(),
            ).collect {
                setState { copy(isProfileMuted = it) }
            }
        }

    private fun observeProfileData() =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId)
                .distinctUntilChanged()
                .collect { profileData ->
                    setState { copy(profileDetails = profileData.asProfileDetailsUi()) }
                }
        }

    private fun observeReferencedProfilesData() =
        viewModelScope.launch {
            profileRepository.observeProfile(profileId)
                .mapNotNull { profile ->
                    profile.metadata?.aboutUris
                        ?.mapNotNull { it.extractProfileId() }
                        ?.filter { it != profileId }
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
                profileRepository.requestProfileUpdate(profileId = profileId)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
    }

    private fun launchReferencedProfilesObserver(profileIds: List<String>) {
        referencedProfilesObserver?.cancel()
        referencedProfilesObserver = viewModelScope.launch {
            profileRepository.observeProfilesData(profileIds = profileIds).collect { profilesData ->
                setState { copy(referencedProfilesData = profilesData.map { it.asProfileDetailsUi() }.toSet()) }
            }
        }
    }

    private fun observeProfileStats() =
        viewModelScope.launch {
            profileRepository.observeProfileStats(profileId = profileId).collect {
                setState {
                    copy(profileStats = it.asProfileStatsUi())
                }
            }
        }

    private fun resolveFollowsMe() {
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

    private suspend fun fetchLatestProfile() =
        try {
            withContext(dispatcherProvider.io()) {
                profileRepository.requestProfileUpdate(profileId = profileId)
            }
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
                profileRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = followAction.profileId,
                    forceUpdate = followAction.forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: ProfileRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileAsUnfollowedAndClearApprovalFlag()
                setState {
                    copy(
                        shouldApproveProfileAction = ProfileApproval.Follow(profileId = followAction.profileId),
                    )
                }
            }
        }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) =
        viewModelScope.launch {
            updateStateProfileAsUnfollowedAndClearApprovalFlag()
            try {
                profileRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = unfollowAction.profileId,
                    forceUpdate = unfollowAction.forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsFollowedAndClearApprovalFlag()
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: ProfileRepository.FollowListNotFound) {
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
                feedsRepository.addFeedLocally(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = buildLatestNotesUserFeedSpec(userId = action.profileId),
                    title = action.feedTitle,
                    description = action.feedDescription,
                    feedSpecKind = FeedSpecKind.Notes,
                    feedKind = FEED_KIND_USER,
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
                setEffect(ProfileDetailsContract.SideEffect.ProfileFeedAdded)
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToAddToFeed(error))
            }
        }
    }

    private fun removeProfileFeed(action: UiEvent.RemoveProfileFeedAction) {
        viewModelScope.launch {
            try {
                feedsRepository.removeFeedLocally(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = buildLatestNotesUserFeedSpec(userId = action.profileId),
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
                setEffect(ProfileDetailsContract.SideEffect.ProfileFeedRemoved)
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToRemoveFeed(error))
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
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToMuteProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToMuteProfile(error))
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
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToUnmuteProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToUnmuteProfile(error))
            }
        }

    private fun reportAbuse(event: UiEvent.ReportAbuse) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    profileRepository.reportAbuse(
                        userId = activeAccountStore.activeUserId(),
                        reportType = event.type,
                        profileId = event.profileId,
                        eventId = event.noteId,
                    )
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun setErrorState(error: ProfileError) {
        setState { copy(error = error) }
    }

    private fun String.resolveProfileId(): String? =
        when {
            this.startsWith("npub") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
            this.startsWith("nprofile1") -> {
                val pubkey = Nip19TLV.parseUriAsNprofileOrNull(this)?.pubkey
                runCatching { pubkey?.bech32ToHexOrThrow() }.getOrNull()
            }

            else -> this
        }
}
