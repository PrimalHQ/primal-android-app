package net.primal.android.explore.search

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.navigation.searchScope
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import timber.log.Timber

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val exploreRepository: ExploreRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(scope = savedStateHandle.searchScope)
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeDebouncedQueryChanges()
        observeRecentUsers()
        fetchRecommendedUsers()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SearchQueryUpdated -> setState { copy(searching = true, searchQuery = it.query) }
                    is UiEvent.ProfileSelected -> markProfileInteraction(profileId = it.profileId)
                    UiEvent.ResetSearchQuery -> setState { copy(searchQuery = "", searchResults = emptyList()) }
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

    private fun observeRecentUsers() {
        viewModelScope.launch {
            exploreRepository.observeRecentUsers()
                .distinctUntilChanged()
                .collect {
                    setState { copy(recentUsers = it.map { it.mapAsUserProfileUi() }) }
                }
        }
    }

    private fun fetchRecommendedUsers() =
        viewModelScope.launch {
            try {
                val popularUsers = withContext(dispatcherProvider.io()) { exploreRepository.fetchPopularUsers() }
                setState { copy(popularUsers = popularUsers.map { it.mapAsUserProfileUi() }) }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun markProfileInteraction(profileId: String) {
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                profileRepository.markAsInteracted(profileId = profileId)
            }
        }
    }
}
