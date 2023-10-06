package net.primal.android.discuss.post

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.discuss.post.NewPostContract.SideEffect
import net.primal.android.discuss.post.NewPostContract.UiEvent
import net.primal.android.discuss.post.NewPostContract.UiState
import net.primal.android.feed.domain.NoteAttachment
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.newPostPreFillContent
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class NewPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _state =
        MutableStateFlow(UiState(preFillContent = savedStateHandle.newPostPreFillContent))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun sendEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PublishPost -> publishPost(it)
                is UiEvent.ImportLocalFiles -> importPhotos(it.uris)
                is UiEvent.DiscardNoteAttachment -> discardAttachment(it.attachmentId)
                is UiEvent.RetryUpload -> retryAttachmentUpload(it.attachmentId)
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(activeAccountAvatarUrl = it.data.pictureUrl)
                }
            }
    }

    private fun publishPost(event: UiEvent.PublishPost) = viewModelScope.launch {
        setState { copy(publishing = true) }
        try {
            val mentionEventTags = event.content.parseEventTags(marker = "mention").toSet()
            val mentionPubkeyTags = event.content.parsePubkeyTags(marker = "mention").toSet()
            val hashtagTags = event.content.parseHashtagTags().toSet()

            val attachments = _state.value.attachments.mapNotNull { it.remoteUrl }
            val refinedContent = if (attachments.isEmpty()) {
                event.content
            } else {
                StringBuilder().apply {
                    append(event.content)
                    appendLine()
                    appendLine()
                    attachments.forEach {
                        append(it)
                        appendLine()
                    }
                }.toString()
            }

            postRepository.publishShortTextNote(
                content = refinedContent,
                tags = mentionEventTags + mentionPubkeyTags + hashtagTags,
            )
            sendEffect(SideEffect.PostPublished)
        } catch (error: NostrPublishException) {
            setErrorState(error = UiState.NewPostError.PublishError(cause = error.cause))
        } catch (error: MissingRelaysException) {
            setErrorState(error = UiState.NewPostError.MissingRelaysConfiguration(cause = error))
        } finally {
            setState { copy(publishing = false) }
        }
    }

    private fun importPhotos(uris: List<Uri>) = viewModelScope.launch {
        val newAttachments = uris.map { NoteAttachment(localUri = it) }
        setState { copy(attachments = attachments + newAttachments) }
        uploadAttachments(attachments = newAttachments)
    }

    private fun uploadAttachments(attachments: List<NoteAttachment>) = viewModelScope.launch {
        attachments.forEach { uploadAttachment(attachment = it) }
        checkUploadQueueAndDisableFlagIfCompleted()
    }

    private suspend fun uploadAttachment(attachment: NoteAttachment) {
        try {
            setState { copy(uploadingAttachments = true) }
            updateNoteAttachmentState(attachment = attachment.copy(uploadError = null))
            val remoteUrl = postRepository.uploadPostAttachment(attachment)
            updateNoteAttachmentState(attachment = attachment.copy(remoteUrl = remoteUrl))
        } catch (error: UnsuccessfulFileUpload) {
            updateNoteAttachmentState(attachment = attachment.copy(uploadError = error))
        }
    }

    private fun updateNoteAttachmentState(attachment: NoteAttachment) {
        setState {
            val attachments = this.attachments.toMutableList()
            val index = attachments.indexOfFirst { attachment.id == it.id }
            if (index != -1) attachments.set(index = index, element = attachment)
            this.copy(attachments = attachments)
        }
    }

    private fun discardAttachment(attachmentId: UUID) = viewModelScope.launch {
        setState {
            val attachments = this.attachments.toMutableList()
            attachments.removeIf { it.id == attachmentId }
            this.copy(
                attachments = attachments,
                uploadingAttachments = if (attachments.isEmpty()) false else this.uploadingAttachments
            )
        }
    }

    private fun retryAttachmentUpload(attachmentId: UUID) = viewModelScope.launch {
        _state.value.attachments.firstOrNull { it.id == attachmentId }?.let {
            uploadAttachment(it)
            checkUploadQueueAndDisableFlagIfCompleted()
        }
    }

    private fun checkUploadQueueAndDisableFlagIfCompleted() {
        val attachments = _state.value.attachments
        val attachmentsInUpload = attachments.count {
            it.uploadError == null && it.remoteUrl == null
        }
        setState { copy(uploadingAttachments = attachmentsInUpload > 0) }
    }

    private fun setErrorState(error: UiState.NewPostError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}
