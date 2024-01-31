package net.primal.android.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.explore.db.TrendingHashtag
import net.primal.android.explore.home.ExploreHomeContract.UiEvent
import net.primal.android.explore.home.ExploreHomeContract.UiState
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import timber.log.Timber

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestTrendingHashtags()
        observeTrendingHashtags()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RefreshTrendingHashtags -> fetchLatestTrendingHashtags()
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(activeAccountAvatarCdnImage = it.avatarCdnImage)
                }
            }
        }

    private fun observeTrendingHashtags() =
        viewModelScope.launch {
            exploreRepository.observeTrendingHashtags()
                .map { data -> data.map { it.asHashtagUi() } }
                .collect {
                    setState { copy(hashtags = it.chunked(3)) }
                }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun fetchLatestTrendingHashtags() =
        viewModelScope.launch {
            setState { copy(refreshing = true) }
            try {
                exploreRepository.fetchTrendingHashtags()
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(refreshing = false) }
            }
        }

    private fun TrendingHashtag.asHashtagUi() = HashtagUi(name = this.hashtag, score = this.score)
}
