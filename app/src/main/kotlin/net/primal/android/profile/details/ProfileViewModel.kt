package net.primal.android.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.details.ProfileContract.UiEvent
import net.primal.android.profile.details.ProfileContract.UiState
import net.primal.android.profile.details.ProfileContract.UiState.ProfileError
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    feedRepository: FeedRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository,
    private val zapHandler: ZapHandler,
    private val settingsRepository: SettingsRepository,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId ?: activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        UiState(
            profileId = profileId,
            isProfileFollowed = false,
            isProfileMuted = false,
            isActiveUser = false,
            isProfileFeedInActiveUserFeeds = false,
            authoredPosts = feedRepository.feedByDirective(feedDirective = "authored;$profileId")
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        fetchLatestProfile()
        fetchLatestMuteList()
        observeEvents()
        observeProfile()
        observeActiveAccount()
        observeMutedAccount()
    }

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
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        isProfileFollowed = it.following.contains(profileId),
                        isActiveUser = it.pubkey == profileId,
                        isProfileFeedInActiveUserFeeds = it.appSettings?.feeds?.any { it.directive == profileId }
                            ?: false,
                        zappingState = this.zappingState.copy(
                            walletConnected = it.hasWallet(),
                            walletPreference = it.walletPreference,
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            walletBalanceInBtc = it.primalWalletBalanceInBtc,
                        ),
                    )
                }
            }
        }

    private fun observeMutedAccount() =
        viewModelScope.launch {
            mutedUserRepository.observeIsUserMuted(pubkey = profileId).collect {
                setState { copy(isProfileMuted = it) }
            }
        }

    private fun observeProfile() =
        viewModelScope.launch {
            profileRepository.observeProfile(profileId = profileId).collect {
                setState {
                    copy(
                        profileDetails = it.metadata?.asProfileDetailsUi() ?: this.profileDetails,
                        profileStats = it.stats?.asProfileStatsUi() ?: this.profileStats,
                    )
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
                postRepository.likePost(
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
                postRepository.repostPost(
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
                setErrorState(error = ProfileError.MissingLightningAddress(IllegalStateException()))
                return@launch
            }

            try {
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

    private fun follow(followAction: UiEvent.FollowAction) =
        viewModelScope.launch {
            try {
                profileRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = followAction.profileId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToFollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            }
        }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) =
        viewModelScope.launch {
            try {
                profileRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = unfollowAction.profileId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
            }
        }

    private fun addUserFeed(action: UiEvent.AddUserFeedAction) =
        viewModelScope.launch {
            try {
                settingsRepository.addAndPersistUserFeed(
                    userId = activeAccountStore.activeUserId(),
                    name = action.name,
                    directive = action.directive,
                )
            } catch (error: WssException) {
                setErrorState(error = ProfileError.FailedToAddToFeed(error))
            }
        }

    private fun removeUserFeed(action: UiEvent.RemoveUserFeedAction) =
        viewModelScope.launch {
            try {
                settingsRepository.removeAndPersistUserFeed(
                    userId = activeAccountStore.activeUserId(),
                    directive = action.directive,
                )
            } catch (error: WssException) {
                setErrorState(error = ProfileError.FailedToRemoveFeed(error))
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                mutedUserRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = action.profileId,
                )
                setState { copy(isProfileMuted = true) }
            } catch (error: NostrPublishException) {
                setErrorState(error = ProfileError.FailedToMuteProfile(error))
            } catch (error: WssException) {
                setErrorState(error = ProfileError.FailedToMuteProfile(error))
            }
        }

    private fun unmute(action: UiEvent.UnmuteAction) =
        viewModelScope.launch {
            try {
                mutedUserRepository.unmuteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    unmutedUserId = action.profileId,
                )
                setState { copy(isProfileMuted = false) }
            } catch (error: NostrPublishException) {
                setErrorState(error = ProfileError.FailedToUnmuteProfile(error))
            }
        }

    private fun setErrorState(error: ProfileError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}
