package net.primal.android.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.explore.db.TrendingHashtag
import net.primal.android.explore.home.ExploreHomeContract.UiState
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val badgesManager: BadgesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        subscribeToActiveAccount()
        observeTrendingHashtags()
        fetchLatestTrendingHashtags()
        subscribeToBadgesUpdates()
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
            badgesManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun fetchLatestTrendingHashtags() =
        viewModelScope.launch {
            try {
                exploreRepository.fetchTrendingHashtags()
            } catch (error: WssException) {
                // Ignore
            }
        }

    private fun TrendingHashtag.asHashtagUi() = HashtagUi(name = this.hashtag, score = this.score)
}
