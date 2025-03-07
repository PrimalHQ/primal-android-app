package net.primal.android.attachments.gallery

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
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.attachments.gallery.MediaGalleryContract.UiEvent
import net.primal.android.attachments.gallery.MediaGalleryContract.UiState
import net.primal.android.attachments.repository.AttachmentsRepository
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.MediaDownloader
import net.primal.android.core.files.error.UnableToSaveContent
import net.primal.android.core.files.error.UnsuccessfulFileDownload
import net.primal.android.navigation.mediaPositionMs
import net.primal.android.navigation.mediaUrl
import net.primal.android.navigation.noteIdOrThrow
import timber.log.Timber

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val mediaDownloader: MediaDownloader,
    private val repository: AttachmentsRepository,
) : ViewModel() {

    private val noteId = savedStateHandle.noteIdOrThrow
    private val initialMediaUrl = savedStateHandle.mediaUrl
    private val initialPositionMs = savedStateHandle.mediaPositionMs

    private val _state = MutableStateFlow(UiState(noteId = noteId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<MediaGalleryContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: MediaGalleryContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        loadAttachments()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SaveMedia -> saveMedia(attachment = it.attachment)
                    is UiEvent.LoadBitmap -> setState { copy(currentDisplayedBitmap = it.bitmap) }
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun loadAttachments() =
        viewModelScope.launch {
            val attachments = withContext(dispatcherProvider.io()) {
                repository.loadAttachments(
                    noteId = noteId,
                    types = listOf(NoteAttachmentType.Image, NoteAttachmentType.Video),
                )
            }
            setState {
                copy(
                    loading = false,
                    attachments = attachments.map { it.asNoteAttachmentUi() },
                    initialAttachmentIndex = attachments.indexOfFirst { it.url == initialMediaUrl },
                    initialPositionMs = this@MediaGalleryViewModel.initialPositionMs,
                )
            }
        }

    private fun saveMedia(attachment: NoteAttachmentUi) =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                try {
                    mediaDownloader.downloadToMediaGallery(url = attachment.url)
                    setEffect(MediaGalleryContract.SideEffect.MediaSaved(type = attachment.type))
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
