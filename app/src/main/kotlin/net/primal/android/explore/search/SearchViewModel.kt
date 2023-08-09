package net.primal.android.explore.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.explore.repository.UserProfileSearchItem
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.explore.search.ui.UserProfileUi
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.db.authorNameUiFriendly
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        observeEvents()
        observeDebouncedQueryChanges()
        fetchRecommendedUsers()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.SearchQueryUpdated -> setState { copy(searchQuery = it.query) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedQueryChanges() = viewModelScope.launch {
        _event.filterIsInstance<UiEvent.SearchQueryUpdated>()
            .debounce(0.42.seconds)
            .collect {
                onSearchQueryChanged(query = it.query)
            }
    }

    private fun onSearchQueryChanged(query: String) = viewModelScope.launch {
        setState { copy(searching = true) }
        try {
            val result = exploreRepository.searchUsers(query = query)
            setState { copy(searchResults = result.map { it.mapAsUserProfileUi() }) }
        } catch (error: WssException) {
            // Ignoring errors for now
        } finally {
            setState { copy(searching = false) }
        }
    }

    private fun fetchRecommendedUsers() = viewModelScope.launch {
        val recommendedUsers = exploreRepository.getRecommendedUsers()
        setState { copy(recommendedUsers = recommendedUsers.map { it.mapAsUserProfileUi() }) }
    }

    private fun UserProfileSearchItem.mapAsUserProfileUi() = UserProfileUi(
        profileId = this.metadata.ownerId,
        displayName = this.metadata.authorNameUiFriendly(),
        internetIdentifier = this.metadata.internetIdentifier,
        avatarUrl = this.metadata.picture,
        followersCount = this.score?.toInt(),
    )
}
