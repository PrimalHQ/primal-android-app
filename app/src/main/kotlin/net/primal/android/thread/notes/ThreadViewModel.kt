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
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.navigation.noteIdOrThrow
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.note.repository.NoteRepository
import net.primal.android.note.ui.asEventZapUiModel
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.thread.notes.ThreadContract.UiEvent
import net.primal.android.thread.notes.ThreadContract.UiState
import timber.log.Timber

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedRepository: FeedRepository,
    private val noteRepository: NoteRepository,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val highlightPostId = savedStateHandle.noteIdOrThrow

    private val _state = MutableStateFlow(UiState(highlightPostId = highlightPostId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeConversationChanges()
        observeTopZappers()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateConversation -> fetchData()
                }
            }
        }

    private fun observeTopZappers() =
        viewModelScope.launch {
            noteRepository.observeTopZappers(eventId = highlightPostId).collect {
                setState { copy(topZaps = it.map { it.asEventZapUiModel() }) }
            }
        }

    private fun observeConversationChanges() =
        viewModelScope.launch {
            var articleObserverStarted = false
            feedRepository.observeConversation(noteId = highlightPostId)
                .filter { it.isNotEmpty() }
                .map { posts -> posts.map { it.asFeedPostUi() } }
                .collect { conversation ->
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
                    feedRepository.fetchReplies(noteId = highlightPostId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetching = false) }
            }
        }

    private fun fetchTopNoteZaps() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    noteRepository.fetchTopNoteZaps(eventId = highlightPostId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
