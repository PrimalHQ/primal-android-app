package net.primal.android.events.gallery

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
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.MediaDownloader
import net.primal.android.core.files.error.UnableToSaveContent
import net.primal.android.core.files.error.UnsuccessfulFileDownload
import net.primal.android.events.gallery.EventMediaGalleryContract.UiEvent
import net.primal.android.events.gallery.EventMediaGalleryContract.UiState
import net.primal.android.navigation.mediaPositionMs
import net.primal.android.navigation.mediaUrl
import net.primal.android.navigation.noteIdOrThrow
import net.primal.domain.EventUriType
import net.primal.domain.repository.EventUriRepository
import timber.log.Timber

@HiltViewModel
class EventMediaGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val mediaDownloader: MediaDownloader,
    private val eventUriRepository: EventUriRepository,
) : ViewModel() {

    private val noteId = savedStateHandle.noteIdOrThrow
    private val initialMediaUrl = savedStateHandle.mediaUrl
    private val initialPositionMs = savedStateHandle.mediaPositionMs

    private val _state = MutableStateFlow(UiState(noteId = noteId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<EventMediaGalleryContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: EventMediaGalleryContract.SideEffect) =
        viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        loadAttachments()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SaveMedia -> saveMedia(attachment = it.attachment)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun loadAttachments() =
        viewModelScope.launch {
            val attachments = withContext(dispatcherProvider.io()) {
                eventUriRepository.loadEventLinks(
                    noteId = noteId,
                    types = listOf(EventUriType.Image, EventUriType.Video),
                )
            }
            setState {
                copy(
                    loading = false,
                    attachments = attachments.map { it.asEventUriUiModel() },
                    initialAttachmentIndex = attachments.indexOfFirst { it.url == initialMediaUrl },
                    initialPositionMs = this@EventMediaGalleryViewModel.initialPositionMs,
                )
            }
        }

    private fun saveMedia(attachment: EventUriUi) =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                try {
                    mediaDownloader.downloadToMediaGallery(url = attachment.url)
                    setEffect(EventMediaGalleryContract.SideEffect.MediaSaved(type = attachment.type))
                } catch (error: UnsuccessfulFileDownload) {
                    Timber.w(error)
                    setState { copy(error = UiState.MediaGalleryError.FailedToSaveMedia(error)) }
                } catch (error: UnableToSaveContent) {
                    Timber.w(error)
                    setState { copy(error = UiState.MediaGalleryError.FailedToSaveMedia(error)) }
                }
            }
        }
}
