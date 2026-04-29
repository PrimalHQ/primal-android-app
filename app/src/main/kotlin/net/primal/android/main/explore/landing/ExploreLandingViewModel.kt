package net.primal.android.main.explore.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.main.explore.landing.ExploreLandingContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository

@HiltViewModel
class ExploreLandingViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeRecentUsers()
        observePopularUsers()
        fetchPopularUsers()
        observeRecentSearches()
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

    private fun observeRecentSearches() =
        viewModelScope.launch {
            exploreRepository.observeRecentSearches(
                ownerId = activeAccountStore.activeUserId(),
                limit = MAX_RECENT_SEARCHES,
            )
                .collect { queries ->
                    setState { copy(recentSearches = queries, recentSearchesLoading = false) }
                }
        }

    companion object {
        private const val MAX_RECENT_SEARCHES = 5
    }
}
