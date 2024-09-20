package net.primal.android.explore.feed.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.android.explore.feed.article.ExploreArticleFeedContract.UiState
import net.primal.android.navigation.exploreFeedSpecOrThrow

@HiltViewModel
class ExploreArticleFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val exploreFeedSpec = savedStateHandle.exploreFeedSpecOrThrow

    private val _state = MutableStateFlow(
        UiState(
            feedSpec = exploreFeedSpec,
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }
}
