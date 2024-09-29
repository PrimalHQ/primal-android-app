package net.primal.android.feeds.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.item.DvmFeedListItemContract.UiEvent
import net.primal.android.feeds.item.DvmFeedListItemContract.UiState

@HiltViewModel(assistedFactory = DvmFeedListItemViewModel.Factory::class)
class DvmFeedListItemViewModel @AssistedInject constructor(
    @Assisted private val dvmFeed: DvmFeed,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(dvmFeed: DvmFeed): DvmFeedListItemViewModel
    }

    private val _state = MutableStateFlow(UiState(dvmFeed = dvmFeed))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val event = MutableSharedFlow<UiEvent>()
    fun setEvent(e: UiEvent) = viewModelScope.launch { event.emit(e) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            event.collect {
                when (it) {
                    UiEvent.OnLikeClick -> onLikeClick()
                    UiEvent.OnZapClick -> onZapClick()
                }
            }
        }

    private fun onLikeClick() =
        viewModelScope.launch {
            // TODO(marko): handle like
        }

    private fun onZapClick() =
        viewModelScope.launch {
            // TODO(marko): handle zap
        }
}
