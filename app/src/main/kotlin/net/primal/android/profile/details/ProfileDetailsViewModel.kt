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
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.feeds.domain.FEED_KIND_USER
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildLatestNotesUserFeedSpec
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.profile.details.ProfileDetailsContract.UiEvent
import net.primal.android.profile.details.ProfileDetailsContract.UiState
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
    private val profileRepository: ProfileRepository,
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
                withContext(dispatcherProvider.io()) {
                    profileRepository.markAsInteracted(profileId = profileId)
                }
            }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowAction -> follow(it)
                    is UiEvent.UnfollowAction -> unfollow(it)
                    is UiEvent.AddUserFeedAction -> addUserFeed(it)
                    is UiEvent.RemoveUserFeedAction -> removeUserFeed(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.UnmuteAction -> unmute(it)
                    UiEvent.RequestProfileUpdate -> requestProfileUpdate()

                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
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
                    )
                }
            }
        }

    private fun observeContainsFeed() =
        viewModelScope.launch {
            val feedSpec = buildLatestNotesUserFeedSpec(userId = profileId)
            feedsRepository.observeContainsFeedSpec(feedSpec = feedSpec).collect {
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
        viewModelScope.launch {
            try {
                feedsRepository.addFeedLocally(
                    feedSpec = buildLatestNotesUserFeedSpec(userId = action.profileId),
                    title = action.feedTitle,
                    description = action.feedDescription,
                    feedSpecKind = FeedSpecKind.Notes,
                    feedKind = FEED_KIND_USER,
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToAddToFeed(error))
            }
        }
    }

    private fun removeUserFeed(action: UiEvent.RemoveUserFeedAction) {
        viewModelScope.launch {
            try {
                feedsRepository.removeFeedLocally(buildLatestNotesUserFeedSpec(userId = action.profileId))
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
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
}
