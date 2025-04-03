package net.primal.android.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.files.MediaDownloader
import net.primal.android.core.files.error.UnableToSaveContent
import net.primal.android.core.files.error.UnsuccessfulFileDownload
import net.primal.android.media.MediaItemContract.SideEffect
import net.primal.android.media.MediaItemContract.UiEvent
import net.primal.android.media.MediaItemContract.UiState
import net.primal.android.navigation.asUrlDecodedNonNullable
import net.primal.android.navigation.mediaUrlOrThrow
import net.primal.core.utils.coroutines.DispatcherProvider
import timber.log.Timber

@HiltViewModel
class MediaItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val mediaDownloader: MediaDownloader,
) : ViewModel() {

    private val mediaUrl = savedStateHandle.mediaUrlOrThrow.asUrlDecodedNonNullable()

    private val _state = MutableStateFlow(UiState(mediaUrl = mediaUrl))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.SaveMedia -> saveMedia()
                }
            }
        }

    private fun saveMedia() =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                try {
                    mediaDownloader.downloadToMediaGallery(url = state.value.mediaUrl)
                    setEffect(SideEffect.MediaSaved)
                } catch (error: UnsuccessfulFileDownload) {
                    Timber.w(error)
                    setState { copy(error = UiState.MediaItemError.FailedToSaveMedia(error)) }
                } catch (error: UnableToSaveContent) {
                    Timber.w(error)
                    setState { copy(error = UiState.MediaItemError.FailedToSaveMedia(error)) }
                }
            }
        }
}
