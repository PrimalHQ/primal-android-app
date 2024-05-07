package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.FileAnalyser
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.editor.NoteEditorContract.SideEffect
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.note.repository.NoteRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import timber.log.Timber

class NoteEditorViewModel @AssistedInject constructor(
    @Assisted content: TextFieldValue,
    @Assisted mediaUri: Uri?,
    @Assisted private val replyToNoteId: String?,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val fileAnalyser: FileAnalyser,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val noteRepository: NoteRepository,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(content = content))
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
        observeDebouncedQueryChanges()

        if (!replyToNoteId.isNullOrEmpty()) {
            fetchRepliesFromNetwork(replyToNoteId)
            observeConversation(replyToNoteId)
        }

        if (mediaUri != null) {
            importPhotos(listOf(mediaUri))
        }

        fetchRecommendedUsers()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.UpdateContent -> setState { copy(content = event.content) }
                    is UiEvent.PublishNote -> publishPost()
                    is UiEvent.ImportLocalFiles -> importPhotos(event.uris)
                    is UiEvent.DiscardNoteAttachment -> discardAttachment(event.attachmentId)
                    is UiEvent.RetryUpload -> retryAttachmentUpload(event.attachmentId)
                    is UiEvent.SearchUsers -> setState { copy(userTaggingQuery = event.query) }
                    is UiEvent.ToggleSearchUsers -> setState {
                        copy(
                            userTaggingQuery = if (event.enabled) "" else null,
                            users = if (event.enabled) this.users else emptyList(),
                        )
                    }

                    is UiEvent.TagUser -> setState {
                        copy(
                            taggedUsers = this.taggedUsers.toMutableList().apply { add(event.taggedUser) },
                        )
                    }
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
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(postId = replyToNoteId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun publishPost() =
        viewModelScope.launch {
            setState { copy(publishing = true) }
            try {
                val rootPost = _state.value.conversation.firstOrNull()
                val replyToPost = _state.value.conversation.lastOrNull()
                val publishedAndImported = noteRepository.publishShortTextNote(
                    content = _state.value.content.text.replaceUserMentionsWithUserIds(
                        users = _state.value.taggedUsers,
                    ),
                    attachments = _state.value.attachments,
                    rootPostId = rootPost?.postId,
                    replyToPostId = replyToPost?.postId,
                    replyToAuthorId = replyToPost?.authorId,
                )

                if (replyToNoteId != null) {
                    if (publishedAndImported) {
                        fetchNoteReplies()
                    } else {
                        scheduleFetchReplies()
                    }
                }

                resetState()

                sendEffect(SideEffect.PostPublished)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiState.NoteEditorError.PublishError(cause = error.cause))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiState.NoteEditorError.MissingRelaysConfiguration(cause = error))
            } finally {
                setState { copy(publishing = false) }
            }
        }

    private fun fetchNoteReplies() {
        if (replyToNoteId != null) {
            fetchRepliesFromNetwork(replyToNoteId)
        }
    }

    private fun scheduleFetchReplies() =
        viewModelScope.launch {
            delay(750.milliseconds)
            fetchNoteReplies()
        }

    private fun String.replaceUserMentionsWithUserIds(users: List<NoteTaggedUser>): String {
        var content = this
        users.forEach { user ->
            content = content.replace(
                oldValue = user.displayUsername,
                newValue = "nostr:${user.userId.hexToNpubHrp()}",
            )
        }
        return content
    }

    private fun resetState() {
        setState {
            copy(
                content = TextFieldValue(),
                attachments = emptyList(),
                users = emptyList(),
                userTaggingQuery = null,
            )
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

            val remoteUrl = withContext(dispatcherProvider.io()) {
                noteRepository.uploadPostAttachment(attachment)
            }
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
            Timber.w(error)
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

    private fun setErrorState(error: UiState.NoteEditorError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedQueryChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.SearchUsers>()
                .debounce(0.42.seconds)
                .collect {
                    searchUserTagging(query = it.query)
                }
        }

    private fun fetchRecommendedUsers() =
        viewModelScope.launch {
            try {
                val recommendedUsers = withContext(dispatcherProvider.io()) { exploreRepository.getRecommendedUsers() }
                setState { copy(recommendedUsers = recommendedUsers.map { it.mapAsUserProfileUi() }) }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun searchUserTagging(query: String) =
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                try {
                    val result = withContext(dispatcherProvider.io()) {
                        exploreRepository.searchUsers(query = query, limit = 10)
                    }
                    setState { copy(users = result.map { it.mapAsUserProfileUi() }) }
                } catch (error: WssException) {
                    Timber.w(error)
                }
            } else {
                setState { copy(users = emptyList()) }
            }
        }
}
