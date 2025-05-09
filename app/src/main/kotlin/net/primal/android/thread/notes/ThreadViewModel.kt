package net.primal.android.thread.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.noteIdOrThrow
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.thread.notes.ThreadContract.UiEvent
import net.primal.android.thread.notes.ThreadContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import net.primal.domain.posts.FeedRepository
import net.primal.domain.reads.ArticleRepository
import timber.log.Timber

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val dispatcherProvider: DispatcherProvider,
    private val feedRepository: FeedRepository,
    private val eventRepository: EventRepository,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val highlightPostId = savedStateHandle.noteIdOrThrow.resolveNoteIdOrThrow()

    private val _state = MutableStateFlow(
        UiState(
            highlightPostId = highlightPostId,
            activeAccountUserId = activeAccountStore.activeUserId(),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeConversationChanges()
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState { copy(activeAccountUserId = activeAccountStore.activeUserId()) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateConversation -> fetchData()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun observeConversationChanges() =
        viewModelScope.launch {
            var articleObserverStarted = false
            feedRepository.observeConversation(userId = activeAccountStore.activeUserId(), noteId = highlightPostId)
                .filter { it.isNotEmpty() }
                .map { posts -> posts.map { it.asFeedPostUi() } }
                .collect { conversation ->
                    setState { copy(highlightNote = conversation.find { it.postId == highlightPostId }) }

                    val highlightPostIndex = conversation.indexOfFirst { it.postId == highlightPostId }
                    if (_state.value.conversation.isEmpty() && highlightPostIndex != -1) {
                        setState {
                            copy(
                                conversation = listOf(conversation[highlightPostIndex]),
                                highlightPostIndex = 0,
                            )
                        }
                        // Delay shortly to propagate highlighted post to UI
                        delay(100.milliseconds)
                    }

                    val thread = conversation.subList(0, highlightPostIndex + 1)
                    val replies = conversation.subList(highlightPostIndex + 1, conversation.size)
                    setState {
                        copy(
                            conversation = thread + replies.sortedByDescending { it.timestamp },
                            highlightPostIndex = highlightPostIndex,
                        )
                    }

                    if (!articleObserverStarted) {
                        articleObserverStarted = true
                        observeArticle()
                    }
                }
        }

    private fun observeArticle() =
        viewModelScope.launch {
            articleRepository.observeArticleByCommentId(commentNoteId = highlightPostId)
                .filterNotNull()
                .collect { article ->
                    setState { copy(replyToArticle = article.mapAsFeedArticleUi()) }
                }
        }

    private fun fetchData() {
        fetchNoteReplies()
        fetchTopNoteZaps()
    }

    private fun fetchNoteReplies() =
        viewModelScope.launch {
            setState { copy(fetching = true) }
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(userId = activeAccountStore.activeUserId(), noteId = highlightPostId)
                }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToFetchNote(error)) }
            } finally {
                setState { copy(fetching = false) }
            }
        }

    private fun fetchTopNoteZaps() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    eventRepository.fetchEventZaps(
                        userId = activeAccountStore.activeUserId(),
                        eventId = highlightPostId,
                        limit = 15,
                    )
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun String.resolveNoteIdOrThrow(): String =
        when {
            this.startsWith("note1") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
            this.startsWith("nevent1") -> Nip19TLV.parseUriAsNeventOrNull(this)?.eventId
            else -> this
        }.toString()
}
