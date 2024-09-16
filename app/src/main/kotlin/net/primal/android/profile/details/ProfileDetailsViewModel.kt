package net.primal.android.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.note.repository.NoteRepository
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.profile.details.ProfileDetailsContract.UiEvent
import net.primal.android.profile.details.ProfileDetailsContract.UiState
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.domain.ProfileFeedSpec
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@Suppress("LongParameterList")
@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository,
    private val zapHandler: ZapHandler,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId ?: activeAccountStore.activeUserId()

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
                withContext(dispatcherProvider.io()) {
                    profileRepository.markAsInteracted(profileId = profileId)
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.FollowAction -> follow(it)
                    is UiEvent.UnfollowAction -> unfollow(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.AddUserFeedAction -> addUserFeed(it)
                    is UiEvent.RemoveUserFeedAction -> removeUserFeed(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.UnmuteAction -> unmute(it)
                    UiEvent.RequestProfileUpdate -> {
                        fetchLatestProfile()
                        fetchLatestMuteList()
                    }

                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    UiEvent.DismissBookmarkConfirmation -> setState { copy(confirmBookmarkingNoteId = null) }
                }
            }
        }

    private fun fetchProfileFollowedBy() =
        viewModelScope.launch {
            try {
                val profiles = profileRepository.fetchUserProfileFollowedBy(
                    profileId = profileId,
                    userId = activeAccountStore.activeUserId(),
                    limit = 10,
                )
                setState { copy(userFollowedByProfiles = profiles.map { it.asProfileDetailsUi() }) }
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
            // TODO Update profile feed spec once api is implemented
            val profileFeedSpec = profileId
            feedRepository.observeContainsFeed(feedSpec = profileFeedSpec).collect {
                setState { copy(isProfileFeedInActiveUserFeeds = it) }
            }
        }

    private fun observeMutedAccount() =
        viewModelScope.launch {
            mutedUserRepository.observeIsUserMuted(pubkey = profileId).collect {
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

    private fun fetchLatestMuteList() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.fetchAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchLatestProfile() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    profileRepository.requestProfileUpdate(profileId = profileId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) =
        viewModelScope.launch {
            try {
                noteRepository.likePost(
                    postId = postLikeAction.postId,
                    postAuthorId = postLikeAction.postAuthorId,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            }
        }

    private fun repostPost(repostAction: UiEvent.RepostAction) =
        viewModelScope.launch {
            try {
                noteRepository.repostPost(
                    postId = repostAction.postId,
                    postAuthorId = repostAction.postAuthorId,
                    postRawNostrEvent = repostAction.postNostrEvent,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(dispatcherProvider.io()) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setErrorState(
                    error = ProfileError.MissingLightningAddress(
                        IllegalStateException("Missing lightning address."),
                    ),
                )
                return@launch
            }

            try {
                withContext(dispatcherProvider.io()) {
                    zapHandler.zap(
                        userId = activeAccountStore.activeUserId(),
                        comment = zapAction.zapDescription,
                        amountInSats = zapAction.zapAmount,
                        target = ZapTarget.Note(
                            zapAction.postId,
                            zapAction.postAuthorId,
                            postAuthorProfileData.lnUrlDecoded,
                        ),
                    )
                }
            } catch (error: ZapFailureException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setErrorState(error = ProfileError.InvalidZapRequest(error))
            }
        }

    private fun updateStateProfileAsFollowed() = setState { copy(isProfileFollowed = true) }

    private fun updateStateProfileAsUnfollowed() = setState { copy(isProfileFollowed = false) }

    private fun follow(followAction: UiEvent.FollowAction) =
        viewModelScope.launch {
            updateStateProfileAsFollowed()
            try {
                profileRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = followAction.profileId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsUnfollowed()
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsUnfollowed()
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsUnfollowed()
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: ProfileRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileAsUnfollowed()
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            }
        }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) =
        viewModelScope.launch {
            updateStateProfileAsUnfollowed()
            try {
                profileRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = unfollowAction.profileId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                updateStateProfileAsFollowed()
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                updateStateProfileAsFollowed()
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                updateStateProfileAsFollowed()
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            } catch (error: ProfileRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileAsFollowed()
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            }
        }

    private fun addUserFeed(action: UiEvent.AddUserFeedAction) {
        setErrorState(error = ProfileError.FailedToRemoveFeed(RuntimeException("Api not implemented")))
        // TODO Implement adding user feed in ProfileDetails
//        viewModelScope.launch {
//            try {
//                settingsRepository.addAndPersistUserFeed(
//                    userId = activeAccountStore.activeUserId(),
//                    name = action.name,
//                    directive = action.directive,
//                )
//            } catch (error: WssException) {
//                Timber.w(error)
//                setErrorState(error = ProfileError.FailedToAddToFeed(error))
//            }
//        }
    }

    private fun removeUserFeed(action: UiEvent.RemoveUserFeedAction) {
        setErrorState(error = ProfileError.FailedToRemoveFeed(RuntimeException("Api not implemented")))
        // TODO Implement removing user feed in ProfileDetails
//        viewModelScope.launch {
//            try {
//                settingsRepository.removeAndPersistUserFeed(
//                    userId = activeAccountStore.activeUserId(),
//                    directive = action.directive,
//                )
//            } catch (error: WssException) {
//                Timber.w(error)
//                setErrorState(error = ProfileError.FailedToRemoveFeed(error))
//            }
//        }
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
                        reportType = event.reportType,
                        profileId = event.profileId,
                        noteId = event.noteId,
                    )
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun handleBookmark(event: UiEvent.BookmarkAction) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            withContext(dispatcherProvider.io()) {
                try {
                    setState { copy(confirmBookmarkingNoteId = null) }
                    val isBookmarked = noteRepository.isBookmarked(noteId = event.noteId)
                    when (isBookmarked) {
                        true -> noteRepository.removeFromBookmarks(
                            userId = userId,
                            forceUpdate = event.forceUpdate,
                            noteId = event.noteId,
                        )

                        false -> noteRepository.addToBookmarks(
                            userId = userId,
                            forceUpdate = event.forceUpdate,
                            noteId = event.noteId,
                        )
                    }
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                } catch (error: ProfileRepository.BookmarksListNotFound) {
                    Timber.w(error)
                    setState { copy(confirmBookmarkingNoteId = event.noteId) }
                }
            }
        }

    private fun setErrorState(error: ProfileError) {
        setState { copy(error = error) }
    }
}
