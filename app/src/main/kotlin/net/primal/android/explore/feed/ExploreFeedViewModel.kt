package net.primal.android.explore.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.ext.removeSearchPrefix
import net.primal.android.explore.feed.ExploreFeedContract.UiState
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.navigation.searchQuery
import javax.inject.Inject

@HiltViewModel
class ExploreFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    feedRepository: FeedRepository,
) : ViewModel() {

    private val exploreQuery = "search;${savedStateHandle.searchQuery}"

    private val _state = MutableStateFlow(
        UiState(
            title = exploreQuery.removeSearchPrefix(),
            posts = feedRepository.feedByDirective(feedDirective = exploreQuery)
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        )
    )
    val state = _state.asStateFlow()

}
