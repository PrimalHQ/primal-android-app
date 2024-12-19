package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.attachments.repository.AttachmentsRepository
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.FileAnalyser
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.editor.NoteEditorContract.SideEffect
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.highlights.model.asHighlightUi
import net.primal.android.highlights.repository.HighlightRepository
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.domain.UploadJob
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import timber.log.Timber

class NoteEditorViewModel @AssistedInject constructor(
    @Assisted private val args: NoteEditorArgs,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val fileAnalyser: FileAnalyser,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val notePublishHandler: NotePublishHandler,
    private val attachmentRepository: AttachmentsRepository,
    private val highlightRepository: HighlightRepository,
    private val exploreRepository: ExploreRepository,
    private val profileRepository: ProfileRepository,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val replyToNoteId = args.replyToNoteId
    private val replyToArticleNaddr = args.replyToArticleNaddr?.let(Nip19TLV::parseUriAsNaddrOrNull)
    private val replyToHighlightId = args.replyToHighlightId

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun sendEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val attachmentUploads = mutableMapOf<UUID, UploadJob>()

    init {
        handleArgs()
        subscribeToEvents()
        subscribeToActiveAccount()
        observeDebouncedQueryChanges()
        observeRecentUsers()
        fetchPopularUsers()
    }

    private fun handleArgs() {
        viewModelScope.launch {
            setStateFromArgs()

            if (!replyToNoteId.isNullOrEmpty()) {
                fetchNoteThreadFromNetwork(replyToNoteId)
                observeThreadConversation(replyToNoteId)
                observeArticleByCommentId(replyToNoteId = replyToNoteId)
            } else if (replyToArticleNaddr != null) {
                fetchArticleDetailsFromNetwork(replyToArticleNaddr)
                observeArticleByNaddr(naddr = replyToArticleNaddr)
            }

            if (replyToHighlightId != null) {
                observeHighlight(highlightId = replyToHighlightId)
            }

            if (args.mediaUris.isNotEmpty()) {
                importPhotos(args.mediaUris.mapNotNull { Uri.parse(it) })
            }
        }
    }

    private fun setStateFromArgs() {
        setState {
            copy(
                content = TextFieldValue(
                    text = args.content,
                    selection = TextRange(
                        start = args.contentSelectionStart,
                        end = args.contentSelectionEnd,
                    ),
                ),
                taggedUsers = args.taggedUsers,
            )
        }
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

                    is UiEvent.TagUser -> {
                        setState {
                            copy(
                                taggedUsers = this.taggedUsers.toMutableList().apply { add(event.taggedUser) },
                            )
                        }
                        markProfileInteraction(profileId = event.taggedUser.userId)
                    }

                    UiEvent.AppendUserTagAtSign -> setState {
                        copy(content = this.content.appendUserTagAtSignAtCursorPosition())
                    }
                }
            }
        }

    private fun observeHighlight(highlightId: String) =
        viewModelScope.launch {
            highlightRepository.observeHighlightById(highlightId = highlightId)
                .collect {
                    setState { copy(replyToHighlight = it.asHighlightUi()) }
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
                            activeAccountLegendaryCustomization = it.data.primalLegendProfile
                                ?.asLegendaryCustomization(),
                        )
                    }
                }
        }

    private fun observeThreadConversation(replyToNoteId: String) {
        viewModelScope.launch {
            feedRepository.observeConversation(noteId = replyToNoteId)
                .filter { it.isNotEmpty() }
                .map { posts -> posts.map { it.asFeedPostUi() } }
                .collect { conversation ->
                    val replyToNoteIndex = conversation.indexOfFirst { it.postId == replyToNoteId }
                    val thread = conversation.subList(0, replyToNoteIndex + 1)
                    setState { copy(conversation = thread) }
                }
        }
    }

    private fun observeArticleByCommentId(replyToNoteId: String) =
        viewModelScope.launch {
            articleRepository.observeArticleByCommentId(commentNoteId = replyToNoteId)
                .filterNotNull()
                .collect { article ->
                    setState { copy(replyToArticle = article.mapAsFeedArticleUi()) }
                }
        }

    private fun observeArticleByNaddr(naddr: Naddr) =
        viewModelScope.launch {
            articleRepository.observeArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
                .filterNotNull()
                .collect { article ->
                    setState { copy(replyToArticle = article.mapAsFeedArticleUi()) }
                }
        }

    private fun fetchNoteThreadFromNetwork(replyToNoteId: String) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(noteId = replyToNoteId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchArticleDetailsFromNetwork(replyToArticleNaddr: Naddr) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    articleRepository.fetchArticleAndComments(
                        articleId = replyToArticleNaddr.identifier,
                        articleAuthorId = replyToArticleNaddr.userId,
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun publishPost() =
        viewModelScope.launch {
            setState { copy(publishing = true) }
            try {
                val article = _state.value.replyToArticle
                val rootPost = _state.value.conversation.firstOrNull()
                val replyToPost = _state.value.conversation.lastOrNull()
                val publishedAndImported = notePublishHandler.publishShortTextNote(
                    userId = activeAccountStore.activeUserId(),
                    content = _state.value.content.text.replaceUserMentionsWithUserIds(
                        users = _state.value.taggedUsers,
                    ),
                    attachments = _state.value.attachments,
                    rootArticleEventId = article?.eventId,
                    rootArticleId = article?.articleId,
                    rootArticleAuthorId = article?.authorId,
                    rootPostId = if (article == null) rootPost?.postId else null,
                    replyToPostId = replyToPost?.postId,
                    rootHighlightId = replyToHighlightId,
                    rootHighlightAuthorId = _state.value.replyToHighlight?.author?.pubkey,
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
            fetchNoteThreadFromNetwork(replyToNoteId)
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

    private fun importPhotos(uris: List<Uri>) {
        val newAttachments = uris.map { NoteAttachment(localUri = it) }
        setState { copy(attachments = attachments + newAttachments) }

        viewModelScope.launch {
            newAttachments
                .map {
                    val uploadId = PrimalFileUploader.generateRandomUploadId()
                    val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
                        uploadAttachment(attachment = it, uploadId = uploadId)
                    }
                    val uploadJob = UploadJob(job = job, id = uploadId)
                    attachmentUploads[it.id] = uploadJob
                    uploadJob
                }.forEach {
                    it.job.start()
                    it.job.join()
                }
            checkUploadQueueAndDisableFlagIfCompleted()
        }
    }

    private suspend fun uploadAttachment(attachment: NoteAttachment, uploadId: String) {
        var updatedAttachment = attachment
        try {
            setState { copy(uploadingAttachments = true) }
            updatedAttachment = updatedAttachment.copy(uploadError = null)
            updateNoteAttachmentState(attachment = updatedAttachment)

            val uploadResult = withContext(dispatcherProvider.io()) {
                attachmentRepository.uploadNoteAttachment(
                    attachment = attachment,
                    uploadId = uploadId,
                    onProgress = { uploadedBytes, totalBytes ->
                        updatedAttachment = updatedAttachment.copy(
                            originalUploadedInBytes = uploadedBytes,
                            originalSizeInBytes = totalBytes,
                        )
                        updateNoteAttachmentState(attachment = updatedAttachment)
                    },
                )
            }

            updatedAttachment = updatedAttachment.copy(
                remoteUrl = uploadResult.remoteUrl,
                originalHash = uploadResult.originalHash,
                originalSizeInBytes = uploadResult.originalFileSize,
            )
            updateNoteAttachmentState(attachment = updatedAttachment)

            val (mimeType, dimensions) = fileAnalyser.extractImageTypeAndDimensions(attachment.localUri)
            if (mimeType != null || dimensions != null) {
                updatedAttachment = updatedAttachment.copy(
                    mimeType = mimeType,
                    dimensionInPixels = dimensions,
                )
                updateNoteAttachmentState(updatedAttachment)
            }
        } catch (error: UnsuccessfulFileUpload) {
            Timber.w(error)
            updateNoteAttachmentState(attachment = updatedAttachment.copy(uploadError = error))
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
            attachmentUploads[attachmentId]?.cancel()
            setState {
                val attachments = this.attachments.toMutableList()
                attachments.removeIf { it.id == attachmentId }
                this.copy(
                    attachments = attachments,
                    uploadingAttachments = if (attachments.isEmpty()) false else this.uploadingAttachments,
                )
            }
        }

    private fun UploadJob?.cancel() {
        if (this == null) return

        viewModelScope.launch {
            this@cancel.job.cancel()
            runCatching {
                attachmentRepository.cancelNoteAttachmentUpload(uploadId = this@cancel.id)
            }
        }
    }

    private fun retryAttachmentUpload(attachmentId: UUID) =
        viewModelScope.launch {
            val noteAttachment = _state.value.attachments.firstOrNull { it.id == attachmentId }
            if (noteAttachment != null) {
                val uploadId = PrimalFileUploader.generateRandomUploadId()
                val job = viewModelScope.launch {
                    uploadAttachment(attachment = noteAttachment, uploadId = uploadId)
                }
                attachmentUploads[attachmentId] = UploadJob(job = job, id = uploadId)
                job.join()
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

    private fun observeRecentUsers() {
        viewModelScope.launch {
            exploreRepository.observeRecentUsers()
                .distinctUntilChanged()
                .collect {
                    setState { copy(recentUsers = it.map { it.mapAsUserProfileUi() }) }
                }
        }
    }

    private fun fetchPopularUsers() =
        viewModelScope.launch {
            try {
                val popularUsers = withContext(dispatcherProvider.io()) { exploreRepository.fetchPopularUsers() }
                setState { copy(popularUsers = popularUsers.map { it.mapAsUserProfileUi() }) }
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

    private fun TextFieldValue.appendUserTagAtSignAtCursorPosition(): TextFieldValue {
        val text = this.text
        val selection = this.selection

        val newText = if (selection.length > 0) {
            text.replaceRange(startIndex = selection.start, endIndex = selection.end, "@")
        } else {
            text.substring(0, selection.start) + "@" + text.substring(selection.start)
        }
        val newSelectionStart = selection.start + 1

        return this.copy(
            text = newText,
            selection = TextRange(start = newSelectionStart, end = newSelectionStart),
        )
    }

    private fun markProfileInteraction(profileId: String) {
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                profileRepository.markAsInteracted(profileId = profileId)
            }
        }
    }
}
