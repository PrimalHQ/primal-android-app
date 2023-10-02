package net.primal.android.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.details.ProfileContract.UiEvent
import net.primal.android.profile.details.ProfileContract.UiState
import net.primal.android.profile.details.ProfileContract.UiState.ProfileError
import net.primal.android.profile.details.model.ProfileDetailsUi
import net.primal.android.profile.details.model.ProfileStatsUi
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.model.ZapTarget
import net.primal.android.wallet.repository.ZapRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository,
    private val zapRepository: ZapRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId ?: activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        UiState(
            profileId = profileId,
            isProfileFollowed = false,
            isActiveUser = false,
            authoredPosts = feedRepository.feedByDirective(feedDirective = "authored;$profileId")
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        observeEvents()
        observeProfile()
        observeActiveAccount()
        fetchLatestProfile()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PostLikeAction -> likePost(it)
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.FollowAction -> follow(it)
                is UiEvent.UnfollowAction -> unfollow(it)
                is UiEvent.ZapAction -> zapPost(it)
                is UiEvent.AddUserFeedAction -> addUserFeed(it)
            }
        }
    }

    private fun observeActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(
                    isProfileFollowed = it.following.contains(profileId),
                    isActiveUser = it.pubkey == profileId,
                    walletConnected = it.nostrWallet != null,
                    defaultZapAmount = it.appSettings?.defaultZapAmount,
                    zapOptions = it.appSettings?.zapOptions ?: emptyList(),
                )
            }
        }
    }

    private fun observeProfile() = viewModelScope.launch {
        profileRepository.observeProfile(profileId = profileId).collect {
            setState {
                copy(
                    profileDetails = if (it.metadata != null) {
                        ProfileDetailsUi(
                            pubkey = it.metadata.ownerId,
                            authorDisplayName = it.metadata.authorNameUiFriendly(),
                            userDisplayName = it.metadata.usernameUiFriendly(),
                            coverUrl = it.metadata.banner,
                            avatarUrl = it.metadata.picture,
                            internetIdentifier = it.metadata.internetIdentifier,
                            about = it.metadata.about,
                            website = it.metadata.website,
                        )
                    } else {
                        this.profileDetails
                    },
                    profileStats = if (it.stats != null) {
                        ProfileStatsUi(
                            followingCount = it.stats.following,
                            followersCount = it.stats.followers,
                            notesCount = it.stats.notes,
                        )
                    } else {
                        this.profileStats
                    },
                    resources = it.resources.map {
                        MediaResourceUi(
                            url = it.url,
                            mimeType = it.contentType,
                            variants = it.variants ?: emptyList(),
                        )
                    },
                )
            }
        }
    }

    private fun fetchLatestProfile() = viewModelScope.launch {
        try {
            profileRepository.requestProfileUpdate(profileId = profileId)
        } catch (error: WssException) {
            // Ignore
        }
    }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) = viewModelScope.launch {
        try {
            postRepository.likePost(
                postId = postLikeAction.postId,
                postAuthorId = postLikeAction.postAuthorId,
            )
        } catch (error: NostrPublishException) {
            setErrorState(error = ProfileError.FailedToPublishLikeEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
        }
    }

    private fun repostPost(repostAction: UiEvent.RepostAction) = viewModelScope.launch {
        try {
            postRepository.repostPost(
                postId = repostAction.postId,
                postAuthorId = repostAction.postAuthorId,
                postRawNostrEvent = repostAction.postNostrEvent,
            )
        } catch (error: NostrPublishException) {
            setErrorState(error = ProfileError.FailedToPublishRepostEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
        }
    }

    private fun zapPost(zapAction: UiEvent.ZapAction) = viewModelScope.launch {
        if (zapAction.postAuthorLightningAddress == null) {
            setErrorState(error = ProfileError.MissingLightningAddress(IllegalStateException()))
            return@launch
        }

        try {
            zapRepository.zap(
                userId = activeAccountStore.activeUserId(),
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.Note(
                    zapAction.postId,
                    zapAction.postAuthorId,
                    zapAction.postAuthorLightningAddress
                ),
            )
        } catch (error: ZapRepository.ZapFailureException) {
            setErrorState(error = ProfileError.FailedToPublishZapEvent(error))
        } catch (error: NostrPublishException) {
            setErrorState(error = ProfileError.FailedToPublishZapEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
        } catch (error: ZapRepository.InvalidZapRequestException) {
            setErrorState(error = ProfileError.InvalidZapRequest(error))
        }
    }

    private fun follow(followAction: UiEvent.FollowAction) = viewModelScope.launch {
        try {
            profileRepository.follow(
                userId = activeAccountStore.activeUserId(),
                followedUserId = followAction.profileId,
            )
        } catch (error: WssException) {
            setErrorState(error = ProfileError.FailedToFollowProfile(error))
        } catch (error: NostrPublishException) {
            setErrorState(error = ProfileError.FailedToFollowProfile(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
        }
    }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) = viewModelScope.launch {
        try {
            profileRepository.unfollow(
                userId = activeAccountStore.activeUserId(),
                unfollowedUserId = unfollowAction.profileId,
            )
        } catch (error: WssException) {
            setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
        } catch (error: NostrPublishException) {
            setErrorState(error = ProfileError.FailedToUnfollowProfile(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = ProfileError.MissingRelaysConfiguration(error))
        }
    }

    private fun addUserFeed(action: UiEvent.AddUserFeedAction) = viewModelScope.launch {
        try {
            settingsRepository.addAndPersistUserFeed(
                userId = activeAccountStore.activeUserId(),
                name = action.name,
                directive = action.directive
            )
        } catch (error: WssException) {
            setErrorState(error = ProfileError.FailedToAddToFeed(error))
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
