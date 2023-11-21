package net.primal.android.attachments.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.attachments.gallery.MediaGalleryContract.UiState
import net.primal.android.attachments.repository.AttachmentsRepository
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.navigation.mediaUrl
import net.primal.android.navigation.noteIdOrThrow

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AttachmentsRepository,
) : ViewModel() {

    private val noteId = savedStateHandle.noteIdOrThrow
    private val initialMediaUrl = savedStateHandle.mediaUrl

    private val _state = MutableStateFlow(UiState(noteId = noteId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        loadAttachments()
    }

    private fun loadAttachments() =
        viewModelScope.launch {
            val attachments = repository.loadAttachments(noteId = noteId, types = listOf(NoteAttachmentType.Image))
            setState {
                copy(
                    loading = false,
                    attachments = attachments.map { it.asNoteAttachmentUi() },
                    initialAttachmentIndex = attachments.indexOfFirst { it.url == initialMediaUrl },
                )
            }
        }
}
