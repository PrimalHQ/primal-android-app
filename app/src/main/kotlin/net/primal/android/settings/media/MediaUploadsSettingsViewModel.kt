package net.primal.android.settings.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiEvent
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiState

@HiltViewModel
class MediaUploadsSettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UpdateMediaUploadsMode -> setState { copy(mode = it.mode) }
                    is UiEvent.UpdateNewBlossomServerUrl -> setState { copy(newBlossomServerUrl = it.url) }
                    is UiEvent.ConfirmBlossomServerUrl -> confirmBlossomServerUrl(it.url)
                    is UiEvent.UpdateNewBlossomMirrorServerUrl -> setState { copy(newBlossomServerMirrorUrl = it.url) }
                    is UiEvent.ConfirmBlossomMirrorServerUrl -> confirmBlossomMirrorServerUrl(it.url)
                    is UiEvent.UpdateBlossomMirrorEnabled -> setState { copy(blossomMirrorEnabled = it.enabled) }
                    UiEvent.RestoreDefaultBlossomServer -> restoreDefaultBlossomServer()
                }
            }
        }

    private fun confirmBlossomMirrorServerUrl(url: String) =
        setState {
            copy(
                blossomServerMirrorUrl = url.removeHttpPrefix(),
                newBlossomServerMirrorUrl = "",
                mode = MediaUploadsMode.View,
            )
        }

    private fun restoreDefaultBlossomServer() =
        setState {
            copy(
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
                blossomServerUrl = DEFAULT_BLOSSOM_URL,
            )
        }

    private fun confirmBlossomServerUrl(url: String) =
        setState {
            copy(
                blossomServerUrl = url.removeHttpPrefix(),
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
            )
        }

    companion object {
        private const val DEFAULT_BLOSSOM_URL = "blossom.primal.net"
    }
}

private fun String.removeHttpPrefix() = this.removePrefix("https://").removePrefix("http://")
