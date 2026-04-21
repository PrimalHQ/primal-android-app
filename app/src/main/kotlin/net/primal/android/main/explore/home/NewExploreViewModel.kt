package net.primal.android.main.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.feeds.DvmFeedListHandler
import net.primal.android.main.explore.home.NewExploreContract.UiState
import net.primal.android.main.explore.people.model.asFollowPackUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository

@HiltViewModel
class NewExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val dvmFeedListHandler: DvmFeedListHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(followPacks = buildFollowPacksPager()),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        fetchPopularUsers()
        fetchAndObserveExploreFeeds()
    }

    private fun fetchPopularUsers() =
        viewModelScope.launch {
            runCatching { exploreRepository.fetchPopularUsers() }
                .onSuccess { users ->
                    setState { copy(popularUsers = users.map { it.mapAsUserProfileUi() }) }
                }
                .onFailure { error ->
                    if (error is NetworkException) {
                        Napier.w(throwable = error) { "Failed to fetch popular users." }
                    }
                }
        }

    private fun buildFollowPacksPager() =
        exploreRepository.getFollowLists()
            .map { pagingData -> pagingData.map { it.asFollowPackUi() } }
            .cachedIn(viewModelScope)

    private fun fetchAndObserveExploreFeeds() =
        viewModelScope.launch {
            runCatching {
                dvmFeedListHandler.fetchDvmFeedsAndObserveStatsUpdates(
                    scope = viewModelScope,
                    userId = activeAccountStore.activeUserId(),
                ) { dvmFeeds -> setState { copy(feeds = dvmFeeds) } }
            }.onFailure { error ->
                if (error is NetworkException) {
                    Napier.w(throwable = error) { "Failed to fetch explore feeds due to network error." }
                }
            }
        }
}
