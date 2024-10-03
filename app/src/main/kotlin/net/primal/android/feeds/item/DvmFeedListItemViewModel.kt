package net.primal.android.feeds.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.item.DvmFeedListItemContract.UiEvent

@HiltViewModel
class DvmFeedListItemViewModel @Inject constructor() : ViewModel() {

    private val event = MutableSharedFlow<UiEvent>()
    fun setEvent(e: UiEvent) = viewModelScope.launch { event.emit(e) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            event.collect {
                when (it) {
                    is UiEvent.OnLikeClick -> onLikeClick(it.dvmFeed)
                    is UiEvent.OnZapClick -> onZapClick(it.dvmFeed)
                }
            }
        }

    private fun onLikeClick(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            // TODO(marko): handle like
        }

    private fun onZapClick(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            // TODO(marko): handle zap
        }
}
