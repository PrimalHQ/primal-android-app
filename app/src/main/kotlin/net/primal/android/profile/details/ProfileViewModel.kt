package net.primal.android.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.profileId
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.profile.db.userNameUiFriendly
import net.primal.android.profile.details.ProfileContract.UiEvent
import net.primal.android.profile.details.ProfileContract.UiState
import net.primal.android.profile.details.model.ProfileDetailsUi
import net.primal.android.profile.details.model.ProfileStatsUi
import net.primal.android.profile.repository.LatestFollowingResolver
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.active.ActiveAccountStore
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val accountsStore: UserAccountsStore,
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId
        ?: activeAccountStore.activeUserAccount.value.pubkey

    private val _state = MutableStateFlow(
        UiState(
            profileId = profileId,
            isActiveUserFollowing = profileId.isFollowed(),
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
        observeActiveAccountChanges()
        fetchLatestProfile()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PostLikeAction -> likePost(it)
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.FollowAction -> follow(it)
                is UiEvent.UnfollowAction -> unfollow(it)
            }
        }
    }

    private fun observeActiveAccountChanges() = viewModelScope.launch {
        accountsStore.userAccounts
            .mapNotNull { it.find { account -> account.pubkey == activeAccountStore.activeUserId() } }
            .collect {
                setState {
                    copy(isActiveUserFollowing = it.following.contains(profileId))
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
                            userDisplayName = it.metadata.userNameUiFriendly(),
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

    private fun String.isFollowed(): Boolean {
        val account = activeAccountStore.activeUserAccount.value
        return account.following.contains(this)
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
            // Propagate error to the UI
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
            // Propagate error to the UI
        }
    }

    private fun follow(followAction: UiEvent.FollowAction) = viewModelScope.launch {
        try {
            profileRepository.follow(followAction.profileId)
        } catch (error: LatestFollowingResolver.RemoteFollowingsUnavailableException) {
            // Failed to retrieve latest contacts, propagate error to the UI
        } catch (error: NostrPublishException) {
            // Failed to publish update, propagate error to the UI
        }
    }

    private fun unfollow(unfollowAction: UiEvent.UnfollowAction) = viewModelScope.launch {
        try {
            profileRepository.unfollow(unfollowAction.profileId)
        } catch (error: LatestFollowingResolver.RemoteFollowingsUnavailableException) {
            // Failed to retrieve latest contacts, propagate error to the UI
        } catch (error: NostrPublishException) {
            // Propagate error to the UI
        }
    }
}
