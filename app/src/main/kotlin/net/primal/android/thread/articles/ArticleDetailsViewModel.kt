package net.primal.android.thread.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticlesRepository
import net.primal.android.navigation.naddrOrThrow
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ArticleDetailsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val readsRepository: ArticlesRepository,
) : ViewModel() {

    private val naddr = Nip19TLV.parseAsNaddr(savedStateHandle.naddrOrThrow)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> fetchData(naddr)
                    UiEvent.DismissErrors -> setState { copy(error = null) }
                }
            }
        }

    private fun fetchData(naddr: Naddr?) =
        viewModelScope.launch {
            if (naddr == null) {
                setState { copy(error = UiState.ArticleDetailsError.InvalidNaddr) }
            } else {
                try {
                    val response = readsRepository.fetchBlogContentAndReplies(
                        userId = activeAccountStore.activeUserId(),
                        authorUserId = naddr.userId,
                        identifier = naddr.identifier,
                    )
                    val content = response.longFormContents.firstOrNull()?.content
                    setState { copy(markdown = content) }

//                    if (content != null) {
//                        val flavour = CommonMarkFlavourDescriptor()
//                        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(content)
//                        val html = HtmlGenerator(content, parsedTree, flavour).generateHtml()
//                        Timber.e("Loaded content = $content")
//                        setState { copy(rawContent = html) }
//                    }
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }
}
