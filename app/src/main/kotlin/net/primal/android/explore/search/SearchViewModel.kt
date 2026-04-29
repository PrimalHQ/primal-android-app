package net.primal.android.explore.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
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
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.navigation.initialQuery
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val initialQuery: String = savedStateHandle.initialQuery.orEmpty()

    private val _state = MutableStateFlow(UiState(searchQuery = initialQuery))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeDebouncedQueryChanges()
        observeRecentUsers()
        observePopularUsers()
        fetchPopularUsers()
        if (initialQuery.isNotEmpty()) {
            setEvent(UiEvent.SearchQueryUpdated(query = initialQuery))
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SearchQueryUpdated -> setState { copy(searching = true, searchQuery = it.query) }
                    is UiEvent.ProfileSelected -> markProfileInteraction(profileId = it.profileId)
                    is UiEvent.SearchSubmitted -> saveRecentSearch(query = it.query)
                    UiEvent.ResetSearchQuery -> setState { copy(searchQuery = "", searchResults = emptyList()) }
                }
            }
        }

    private fun saveRecentSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            runCatching {
                exploreRepository.saveRecentSearch(
                    ownerId = activeAccountStore.activeUserId(),
                    query = query.trim(),
                )
            }.onFailure { error ->
                Napier.w(throwable = error) { "Failed to save recent search." }
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
                val result = exploreRepository.searchUsers(query = query)
                setState { copy(searchResults = result.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to search users with query: $query" }
            } finally {
                setState { copy(searching = false) }
            }
        }

    private fun observeRecentUsers() =
        viewModelScope.launch {
            userRepository.observeRecentUsers(ownerId = activeAccountStore.activeUserId())
                .collect { users ->
                    setState { copy(recentUsers = users) }
                }
        }

    private fun observePopularUsers() =
        viewModelScope.launch {
            exploreRepository.observePopularUsers()
                .collect { users ->
                    setState { copy(popularUsers = users.map { it.mapAsUserProfileUi() }) }
                }
        }

    private fun fetchPopularUsers() =
        viewModelScope.launch {
            runCatching { exploreRepository.fetchPopularUsers() }
                .onFailure { error ->
                    if (error is NetworkException) {
                        Napier.w(throwable = error) { "Failed to fetch popular users." }
                    }
                }
        }

    private fun markProfileInteraction(profileId: String) {
        viewModelScope.launch {
            userRepository.markAsInteracted(profileId = profileId, ownerId = activeAccountStore.activeUserId())
        }
    }
}
