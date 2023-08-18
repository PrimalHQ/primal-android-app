package net.primal.android.discuss.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.discuss.feed.FeedContract.UiEvent
import net.primal.android.discuss.feed.FeedContract.UiState
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.feedDirective
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.api.MalformedLightningAddressException
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.nostr.repository.ZapRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class FeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val zapRepository: ZapRepository
) : ViewModel() {

    private val feedDirective: String = savedStateHandle.feedDirective ?: "network;trending"

    private var userDataUpdater: UserDataUpdater? = null

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedByDirective(feedDirective = feedDirective)
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
        subscribeToFeedTitle()
        subscribeToEvents()
        subscribeToFeedSyncUpdates()
        subscribeToActiveAccount()
    }

    private fun subscribeToFeedTitle() = viewModelScope.launch {
        feedRepository.observeFeedByDirective(feedDirective = feedDirective).collect {
            setState {
                copy(feedTitle = it?.name ?: feedDirective.ellipsizeMiddle(size = 8))
            }
        }
    }

    private fun subscribeToFeedSyncUpdates() = viewModelScope.launch {
        feedRepository.observeNewFeedPostsSyncUpdates(
            feedDirective = feedDirective,
            since = Instant.now().epochSecond
        ).collect { syncData ->
            val limit = if (syncData.count <= 3) syncData.count else 3
            val newPosts = withContext(Dispatchers.IO) {
                feedRepository.findNewestPosts(
                    feedDirective = feedDirective,
                    limit = syncData.count
                )
                    .filter { it.author?.picture != null }
                    .distinctBy { it.author?.ownerId }
                    .take(limit)
            }
            setState {
                copy(
                    syncStats = FeedPostsSyncStats(
                        postsCount = this.syncStats.postsCount + syncData.count,
                        postIds = this.syncStats.postIds + syncData.postIds,
                        avatarUrls = newPosts.mapNotNull { feedPost ->
                            feedPost.author?.picture
                        }
                    )
                )
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                userDataUpdater = userDataSyncerFactory.create(userId = it.data.pubkey)
                setState {
                    copy(
                        activeAccountAvatarUrl = it.data.pictureUrl,
                        walletConnected = it.data.nostrWallet != null,
                    )
                }
            }
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                UiEvent.FeedScrolledToTop -> clearSyncStats()
                UiEvent.RequestUserDataUpdate -> updateUserData()
                is UiEvent.PostLikeAction -> likePost(it)
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.ZapAction -> zapPost(it)
            }
        }
    }

    private fun clearSyncStats() {
        setState {
            copy(
                syncStats = this.syncStats.copy(
                    postIds = emptyList(),
                    postsCount = 0
                )
            )
        }
    }

    private fun updateUserData() = viewModelScope.launch {
        userDataUpdater?.updateUserDataWithDebounce(timeoutInSeconds = 30.minutes.inWholeSeconds)
    }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) = viewModelScope.launch {
        try {
            postRepository.likePost(
                postId = postLikeAction.postId,
                postAuthorId = postLikeAction.postAuthorId,
            )
        } catch (error: NostrPublishException) {
            setState { copy( error = FeedContract.PostActionError.FailedToPublishLikeEvent ) }
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
            setState { copy( error = FeedContract.PostActionError.FailedToPublishRepostEvent ) }
        }
    }

    private fun zapPost(zapAction: UiEvent.ZapAction) = viewModelScope.launch {
        try {
            if (zapAction.postAuthorLightningAddress == null) {
                setState { copy( error = FeedContract.PostActionError.MissingLightningAddress ) }
                return@launch
            }

            val amount = zapAction.zapAmount ?: 42

            zapRepository.zap(
                zapAction.zapDescription ?: "",
                amount,
                ZapTarget.Note(zapAction.postId, zapAction.postAuthorId, zapAction.postAuthorLightningAddress),
                activeAccountStore.activeUserAccount().relays
            )
        } catch (error: MalformedLightningAddressException) {
            setState { copy( error = FeedContract.PostActionError.MalformedLightningAddress ) }
        } catch (error: NostrPublishException) {
            setState { copy( error = FeedContract.PostActionError.FailedToPublishZapEvent ) }
        }
    }

}
