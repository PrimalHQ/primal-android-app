package net.primal.android.explore.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.ext.removeSearchPrefix
import net.primal.android.discuss.feed.FeedContract
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent
import net.primal.android.explore.feed.ExploreFeedContract.UiState
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.searchQuery
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.api.MalformedLightningAddressException
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.nostr.repository.ZapRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import javax.inject.Inject

@HiltViewModel
class ExploreFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val zapRepository: ZapRepository
) : ViewModel() {

    private val exploreQuery = "search;\"${savedStateHandle.searchQuery}\""

    private val _state = MutableStateFlow(
        UiState(
            title = exploreQuery.removeSearchPrefix(),
            posts = feedRepository.feedByDirective(feedDirective = exploreQuery)
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
        observeContainsFeed()
        observeEvents()
        observeActiveAccount()
    }

    private fun observeContainsFeed() = viewModelScope.launch {
        feedRepository.observeContainsFeed(directive = exploreQuery).collect {
            setState {
                copy(existsInUserFeeds = it)
            }
        }
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                UiEvent.AddToUserFeeds -> addToMyFeeds()
                UiEvent.RemoveFromUserFeeds -> removeFromMyFeeds()
                is UiEvent.PostLikeAction -> likePost(it)
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.ZapAction -> zapPost(it)
            }
        }
    }

    private fun observeActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(walletConnected = it.data.nostrWallet != null)
                }
            }
    }

    private suspend fun addToMyFeeds() {
        feedRepository.addToUserFeeds(title = state.value.title, directive = exploreQuery)
    }

    private suspend fun removeFromMyFeeds() {
        feedRepository.removeFromUserFeeds(directive = exploreQuery)
    }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) = viewModelScope.launch {
        try {
            postRepository.likePost(
                postId = postLikeAction.postId,
                postAuthorId = postLikeAction.postAuthorId,
            )
        } catch (error: NostrPublishException) {
            setState { copy( error = ExploreFeedContract.PostActionError.FailedToPublishLikeEvent ) }
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
            setState { copy( error = ExploreFeedContract.PostActionError.FailedToPublishRepostEvent ) }
        }
    }

    private fun zapPost(zapAction: UiEvent.ZapAction) = viewModelScope.launch {
        try {
            if (zapAction.postAuthorLightningAddress == null) {
                setState { copy( error = ExploreFeedContract.PostActionError.MissingLightningAddress ) }
                return@launch
            }

            val amount = zapAction.zapAmount ?: 42
            val activeAccount = activeAccountStore.activeUserAccount()

            if (activeAccount.nostrWallet == null) {
                return@launch
            }

            zapRepository.zap(
                userId = activeAccount.pubkey,
                comment = zapAction.zapDescription ?: "",
                amount = amount,
                target = ZapTarget.Note(zapAction.postId, zapAction.postAuthorId, zapAction.postAuthorLightningAddress),
                relays = activeAccount.relays,
                nostrWallet = activeAccount.nostrWallet,
            )
        } catch (error: MalformedLightningAddressException) {
            setState { copy( error = ExploreFeedContract.PostActionError.MalformedLightningAddress ) }
        } catch (error: Exception) {
            when (error) {
                is ZapRepository.ZapFailureException, is NostrPublishException -> {
                    setState { copy( error = ExploreFeedContract.PostActionError.FailedToPublishZapEvent ) }
                }
                else -> throw error
            }
        }
    }
}
