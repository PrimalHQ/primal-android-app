package net.primal.android.notes.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository

@HiltViewModel(assistedFactory = NoteFeedViewModel.Factory::class)
class NoteFeedViewModel @AssistedInject constructor(
    @Assisted private val feedSpec: String,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedSpec: String): NoteFeedViewModel
    }

    private fun buildFeedByDirective() =
        feedRepository.feedByDirective(feedDirective = feedSpec)
            .map { it.map { feedNote -> feedNote.asFeedPostUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(NoteFeedContract.UiState(notes = buildFeedByDirective()))
    val state = _state.asStateFlow()
}
