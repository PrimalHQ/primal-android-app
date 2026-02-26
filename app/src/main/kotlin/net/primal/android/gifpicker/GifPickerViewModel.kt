package net.primal.android.gifpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.errors.UiError
import net.primal.android.gifpicker.GifPickerContract.SideEffect
import net.primal.android.gifpicker.GifPickerContract.UiEvent
import net.primal.android.gifpicker.GifPickerContract.UiState
import net.primal.android.gifpicker.domain.GifCategory
import net.primal.android.gifpicker.domain.asGifItem
import net.primal.data.remote.api.klipy.KlipyApi

@HiltViewModel
class GifPickerViewModel @Inject constructor(
    private val klipyApi: KlipyApi,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    companion object {
        private val SEARCH_DEBOUNCE_DURATION = 0.42.seconds
    }

    private var nextCursor: String? = null

    init {
        fetchTrending()
        observeEvents()
        observeDebouncedSearchQuery()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.UpdateSearchQuery -> {
                        setState { copy(searchQuery = event.query) }
                    }

                    is UiEvent.SelectCategory -> {
                        val newCategory = if (_state.value.selectedCategory == event.category) {
                            GifCategory.TRENDING
                        } else {
                            event.category
                        }
                        if (newCategory == _state.value.selectedCategory) return@collect
                        nextCursor = null
                        setState {
                            copy(
                                searchQuery = "",
                                selectedCategory = newCategory,
                                gifItems = emptyList(),
                            )
                        }
                        if (newCategory == GifCategory.TRENDING) {
                            fetchTrending()
                        } else {
                            searchGifs(query = newCategory.displayName)
                        }
                    }

                    is UiEvent.SelectGif -> selectGif(event)

                    is UiEvent.LoadMoreGifs -> loadMoreGifs()

                    is UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedSearchQuery() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.UpdateSearchQuery>()
                .debounce(SEARCH_DEBOUNCE_DURATION)
                .collectLatest {
                    nextCursor = null
                    if (it.query.isBlank()) {
                        setState {
                            copy(
                                gifItems = emptyList(),
                                selectedCategory = GifCategory.TRENDING,
                            )
                        }
                        performFetchTrending()
                    } else {
                        setState {
                            copy(
                                gifItems = emptyList(),
                                selectedCategory = null,
                            )
                        }
                        performSearchGifs(query = it.query)
                    }
                }
        }

    private fun fetchTrending(cursor: String? = null) = viewModelScope.launch { performFetchTrending(cursor) }

    private suspend fun performFetchTrending(cursor: String? = null) {
        setState { copy(searching = true) }
        runCatching { klipyApi.fetchTrendingGifs(cursor = cursor) }
            .onSuccess { response ->
                val gifs = response.results.mapNotNull { it.asGifItem() }
                nextCursor = response.next
                setState {
                    copy(
                        gifItems = if (cursor == null) gifs else (gifItems + gifs).distinctBy { it.id },
                        searching = false,
                    )
                }
            }
            .onFailure { error ->
                Napier.w(throwable = error) { "Failed to fetch trending GIFs" }
                setState { copy(searching = false, error = UiError.GenericError()) }
            }
    }

    private fun searchGifs(query: String, cursor: String? = null) =
        viewModelScope.launch { performSearchGifs(query, cursor) }

    private suspend fun performSearchGifs(query: String, cursor: String? = null) {
        setState { copy(searching = true) }
        runCatching { klipyApi.searchGifs(query = query, cursor = cursor) }
            .onSuccess { response ->
                val gifs = response.results.mapNotNull { it.asGifItem() }
                nextCursor = response.next
                setState {
                    copy(
                        gifItems = if (cursor == null) gifs else (gifItems + gifs).distinctBy { it.id },
                        searching = false,
                    )
                }
            }
            .onFailure { error ->
                Napier.w(throwable = error) { "Failed to search GIFs" }
                setState { copy(searching = false, error = UiError.GenericError()) }
            }
    }

    private fun loadMoreGifs() =
        viewModelScope.launch {
            val currentState = _state.value
            val cursor = nextCursor
            if (currentState.searching || cursor == null) return@launch

            val query = currentState.searchQuery
            val category = currentState.selectedCategory

            when {
                query.isNotBlank() -> searchGifs(query = query, cursor = cursor)
                category != null && category != GifCategory.TRENDING -> {
                    searchGifs(query = category.displayName, cursor = cursor)
                }
                else -> fetchTrending(cursor = cursor)
            }
        }

    private fun selectGif(event: UiEvent.SelectGif) {
        setEffect(SideEffect.GifSelected(url = event.gif.url))
        viewModelScope.launch {
            withContext(NonCancellable) {
                runCatching {
                    klipyApi.registerShare(gifId = event.gif.id, query = _state.value.searchQuery)
                }
            }
        }
    }
}
