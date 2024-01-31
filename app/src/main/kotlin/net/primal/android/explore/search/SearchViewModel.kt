package net.primal.android.explore.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeDebouncedQueryChanges()
        fetchRecommendedUsers()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SearchQueryUpdated -> setState {
                        copy(searching = true, searchQuery = it.query)
                    }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedQueryChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.SearchQueryUpdated>()
                .debounce(0.42.seconds)
                .collect {
                    onSearchQueryChanged(query = it.query)
                }
        }

    private fun onSearchQueryChanged(query: String) =
        viewModelScope.launch {
            setState { copy(searching = true) }
            try {
                val result = withContext(dispatcherProvider.io()) { exploreRepository.searchUsers(query = query) }
                setState { copy(searchResults = result.map { it.mapAsUserProfileUi() }) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(searching = false) }
            }
        }

    private fun fetchRecommendedUsers() =
        viewModelScope.launch {
            val recommendedUsers = withContext(dispatcherProvider.io()) { exploreRepository.getRecommendedUsers() }
            setState { copy(recommendedUsers = recommendedUsers.map { it.mapAsUserProfileUi() }) }
        }
}
