package net.primal.android.settings.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.primal.android.settings.content.ContentDisplaySettingsContract.UiEvent
import net.primal.android.settings.content.ContentDisplaySettingsContract.UiState

class ContentDisplaySettingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = viewModelScope.launch { _uiState.update(reducer) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UpdateAutoPlayVideos -> handleAutoPlayVideosUpdate(it)
                    is UiEvent.UpdateShowAnimatedAvatars -> handleShowAnimatedAvatarsUpdate(it)
                    is UiEvent.UpdateShowFocusMode -> handleShowFocusModeUpdate(it)
                }
            }
        }
    }

    private fun handleAutoPlayVideosUpdate(event: UiEvent.UpdateAutoPlayVideos) {
        setState { copy(autoPlayVideos = event.code) }
    }

    private fun handleShowAnimatedAvatarsUpdate(event: UiEvent.UpdateShowAnimatedAvatars) {
        setState { copy(showAnimatedAvatars = event.enabled) }
    }

    private fun handleShowFocusModeUpdate(event: UiEvent.UpdateShowFocusMode) {
        setState { copy(focusMode = event.enabled) }
    }
}
