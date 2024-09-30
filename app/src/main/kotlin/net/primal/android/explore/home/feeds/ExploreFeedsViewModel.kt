package net.primal.android.explore.home.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.home.feeds.ExploreFeedsContract.UiState
import net.primal.android.feeds.repository.FeedsRepository
import timber.log.Timber

@HiltViewModel(assistedFactory = ExploreFeedsViewModel.Factory::class)
class ExploreFeedsViewModel @AssistedInject constructor(
    @Assisted private val activeAccountPubkey: String?,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activeAccountPubkey: String?): ExploreFeedsViewModel
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        fetchExploreFeeds()
    }

    private fun fetchExploreFeeds() =
        viewModelScope.launch {
            runCatching {
                val feeds = feedsRepository.fetchRecommendedDvmFeeds(pubkey = activeAccountPubkey)
                setState { copy(feeds = feeds) }
            }.onFailure {
                Timber.w(it)
            }
        }
}
