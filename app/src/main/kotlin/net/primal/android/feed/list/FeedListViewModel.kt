package net.primal.android.feed.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.feed.db.Feed
import net.primal.android.feed.list.FeedListContract.UiState
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.shared.model.FeedUi
import javax.inject.Inject

@HiltViewModel
class FeedListViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

     init {
        observeFeeds()
    }

    private fun observeFeeds() = viewModelScope.launch {
        feedRepository.observeFeeds().collect { feeds ->
            setState {
                copy(feeds = feeds.map { it.asFeedUi() })
            }
        }
    }

    private fun Feed.asFeedUi() = FeedUi(directive = this.directive, name = this.name)

}