package net.primal.android.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.files.FileAnalyser
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.editor.NoteEditorContract.SideEffect
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.newPostPreFillContent
import net.primal.android.navigation.newPostPreFillFileUri
import net.primal.android.navigation.replyToNoteId
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val fileAnalyser: FileAnalyser,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val argReplyToNoteId = savedStateHandle.replyToNoteId
    private val argPreFillFileUri = savedStateHandle.newPostPreFillFileUri?.let { Uri.parse(it) }
    private val argPreFillContent = savedStateHandle.newPostPreFillContent

    private val _state = MutableStateFlow(UiState(preFillContent = argPreFillContent))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun sendEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()

        if (argReplyToNoteId != null) {
            fetchRepliesFromNetwork(argReplyToNoteId)
            observeConversation(argReplyToNoteId)
        }

        if (argPreFillFileUri != null) {
            importPhotos(listOf(argPreFillFileUri))
        }
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.PublishPost -> publishPost(it)
                    is UiEvent.ImportLocalFiles -> importPhotos(it.uris)
                    is UiEvent.DiscardNoteAttachment -> discardAttachment(it.attachmentId)
                    is UiEvent.RetryUpload -> retryAttachmentUpload(it.attachmentId)
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState
                .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
                .collect {
                    setState {
                        copy(
                            activeAccountAvatarCdnImage = it.data.avatarCdnImage,
                        )
                    }
                }
        }

    private fun observeConversation(replyToNoteId: String) =
        viewModelScope.launch {
            feedRepository.observeConversation(postId = replyToNoteId)
                .filter { it.isNotEmpty() }
                .map { posts -> posts.map { it.asFeedPostUi() } }
                .collect { conversation ->
                    val replyToNoteIndex = conversation.indexOfFirst { it.postId == replyToNoteId }
                    val thread = conversation.subList(0, replyToNoteIndex + 1)
                    setState {
                        copy(
                            conversation = thread,
                        )
                    }
                }
        }

    private fun fetchRepliesFromNetwork(replyToNoteId: String) =
        viewModelScope.launch {
            try {
                feedRepository.fetchReplies(postId = replyToNoteId)
            } catch (error: WssException) {
                // Ignore
            }
        }

    private fun publishPost(event: UiEvent.PublishPost) =
        viewModelScope.launch {
            setState { copy(publishing = true) }
            try {
                val rootPost = _state.value.conversation.firstOrNull()
                val replyToPost = _state.value.conversation.lastOrNull()
                postRepository.publishShortTextNote(
                    content = event.content,
                    attachments = _state.value.attachments,
                    rootPostId = rootPost?.postId,
                    replyToPostId = replyToPost?.postId,
                    replyToAuthorId = replyToPost?.authorId,
                )
                sendEffect(SideEffect.PostPublished)
            } catch (error: NostrPublishException) {
                setErrorState(error = UiState.NewPostError.PublishError(cause = error.cause))
            } catch (error: MissingRelaysException) {
                setErrorState(
                    error = UiState.NewPostError.MissingRelaysConfiguration(cause = error),
                )
            } finally {
                setState { copy(publishing = false) }
            }
        }

    private fun importPhotos(uris: List<Uri>) =
        viewModelScope.launch {
            val newAttachments = uris.map { NoteAttachment(localUri = it) }
            setState { copy(attachments = attachments + newAttachments) }
            uploadAttachments(attachments = newAttachments)
        }

    private fun uploadAttachments(attachments: List<NoteAttachment>) =
        viewModelScope.launch {
            attachments.forEach { uploadAttachment(attachment = it) }
            checkUploadQueueAndDisableFlagIfCompleted()
        }

    private suspend fun uploadAttachment(attachment: NoteAttachment) {
        try {
            setState { copy(uploadingAttachments = true) }
            updateNoteAttachmentState(attachment = attachment.copy(uploadError = null))

            val remoteUrl = postRepository.uploadPostAttachment(attachment)
            updateNoteAttachmentState(attachment = attachment.copy(remoteUrl = remoteUrl))

            if (attachment.isImageAttachment) {
                val (mimeType, dimensions) = fileAnalyser.extractImageTypeAndDimensions(
                    attachment.localUri,
                )
                updateNoteAttachmentState(
                    attachment = attachment.copy(
                        mimeType = mimeType,
                        otherRelevantInfo = dimensions,
                    ),
                )
            }
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

    private fun discardAttachment(attachmentId: UUID) =
        viewModelScope.launch {
            setState {
                val attachments = this.attachments.toMutableList()
                attachments.removeIf { it.id == attachmentId }
                this.copy(
                    attachments = attachments,
                    uploadingAttachments = if (attachments.isEmpty()) false else this.uploadingAttachments,
                )
            }
        }

    private fun retryAttachmentUpload(attachmentId: UUID) =
        viewModelScope.launch {
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
