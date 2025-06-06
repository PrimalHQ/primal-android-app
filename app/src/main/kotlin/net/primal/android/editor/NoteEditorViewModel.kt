package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.net.toUri
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
import net.primal.android.articles.feed.ui.generateNaddr
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.articles.highlights.asHighlightUi
import net.primal.android.articles.highlights.generateNevent
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.files.FileAnalyser
import net.primal.android.editor.NoteEditorContract.ReferencedUri
import net.primal.android.editor.NoteEditorContract.SideEffect
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.core.networking.blossom.UploadJob
import net.primal.core.networking.blossom.UploadResult
import net.primal.core.utils.fetchAndGet
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.nostr.MAX_RELAY_HINTS
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.Nip19TLV.toNprofileString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.Nprofile
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.utils.parseNostrUris
import net.primal.domain.nostr.utils.takeAsNaddrOrNull
import net.primal.domain.nostr.utils.takeAsNeventOrNull
import net.primal.domain.nostr.utils.withNostrPrefix
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedRepository
import net.primal.domain.reads.Article
import net.primal.domain.reads.ArticleRepository
import net.primal.domain.reads.HighlightRepository
import timber.log.Timber

class NoteEditorViewModel @AssistedInject constructor(
    @Assisted private val args: NoteEditorArgs,
    private val fileAnalyser: FileAnalyser,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val notePublishHandler: NotePublishHandler,
    private val primalUploadService: AndroidPrimalBlossomUploadService,
    private val highlightRepository: HighlightRepository,
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val relayRepository: RelayRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
) : ViewModel() {

    private val referencedArticleNaddr = args.referencedArticleNaddr?.let(Nip19TLV::parseUriAsNaddrOrNull)
    private val referencedHighlightNevent = args.referencedHighlightNevent?.let(Nip19TLV::parseUriAsNeventOrNull)
    private val referencedNoteNevent = args.referencedNoteNevent?.let(Nip19TLV::parseUriAsNeventOrNull)

    private val _state = MutableStateFlow(UiState(isQuoting = args.isQuoting))
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

            if (args.isQuoting) {
                processQuotedEntities()
            } else {
                fetchReplyToEntities()
            }

            if (args.mediaUris.isNotEmpty()) {
                importPhotos(args.mediaUris.map { it.toUri() })
            }
        }
    }

    private fun processQuotedEntities() =
        viewModelScope.launch {
            val referencedUris = listOfNotNull(
                referencedNoteNevent?.let {
                    ReferencedUri.Note(
                        data = null,
                        loading = true,
                        uri = it.toNeventString(),
                        nevent = it,
                    )
                },
                referencedHighlightNevent?.let {
                    ReferencedUri.Highlight(
                        data = null,
                        loading = true,
                        uri = it.toNeventString(),
                        nevent = it,
                    )
                },
                referencedArticleNaddr?.let {
                    ReferencedUri.Article(
                        data = null,
                        loading = true,
                        uri = it.toNaddrString(),
                        naddr = it,
                    )
                },
            )

            setState { copy(referencedNostrUris = referencedNostrUris + referencedUris) }
            fetchNostrUris(uris = referencedUris)
        }

    private fun fetchReplyToEntities() {
        if (referencedNoteNevent != null) {
            fetchNoteThreadFromNetwork(replyToNoteId = referencedNoteNevent.eventId)
            observeThreadConversation(replyToNoteId = referencedNoteNevent.eventId)
            observeArticleByCommentId(replyToNoteId = referencedNoteNevent.eventId)
        } else if (referencedArticleNaddr != null) {
            fetchArticleDetailsFromNetwork(replyToArticleNaddr = referencedArticleNaddr)
            observeArticleByNaddr(naddr = referencedArticleNaddr)
        }

        if (referencedHighlightNevent != null) {
            observeHighlight(highlightNevent = referencedHighlightNevent)
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

    @Suppress("CyclomaticComplexMethod")
    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.UpdateContent -> setState { copy(content = event.content) }
                    is UiEvent.PasteContent -> handlePasteContent(content = event.content)
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

                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.RefreshUri -> fetchNostrUris(
                        uris = state.value.referencedNostrUris.filter { it.uri == event.uri },
                    )

                    is UiEvent.RemoveUri -> setState {
                        copy(
                            referencedNostrUris = referencedNostrUris
                                .filterIndexed { index, _ -> index != event.uriIndex },
                        )
                    }

                    is UiEvent.RemoveHighlightByArticle ->
                        setState {
                            copy(
                                referencedNostrUris = referencedNostrUris.filter {
                                    it !is ReferencedUri.Highlight || it.data?.referencedEventATag != event.articleATag
                                },
                            )
                        }
                }
            }
        }

    private fun handlePasteContent(content: TextFieldValue) =
        viewModelScope.launch {
            val uris = content.text.parseNostrUris() + content.text.split(" ").filter { it.isLnInvoice() }
            var contentText = content.text

            uris.mapNotNull(::mapUriToReferencedUri)
                .onEach { contentText = contentText.replace(it.uri, "") }
                .also {
                    fetchNostrUris(it)
                    setState { copy(referencedNostrUris = it + referencedNostrUris) }
                }

            setState { copy(content = content.copy(text = contentText)) }
        }

    private fun fetchNostrUris(uris: List<ReferencedUri<*>>) =
        viewModelScope.launch {
            uris.onEach {
                when (it) {
                    is ReferencedUri.Article ->
                        fetchAndUpdateArticleUriDetails(it.uri, it.naddr)

                    is ReferencedUri.Note ->
                        fetchAndUpdateNoteUriDetails(it.uri, it.nevent)

                    is ReferencedUri.Highlight ->
                        getAndUpdateHighlightUriDetails(it.uri, it.nevent)

                    is ReferencedUri.LightningInvoice -> Unit
                }
            }
        }

    private fun observeHighlight(highlightNevent: Nevent) =
        viewModelScope.launch {
            highlightRepository.observeHighlightById(highlightId = highlightNevent.eventId)
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
                            activeAccountBlossoms = it.data.blossomServers,
                        )
                    }
                }
        }

    private fun observeThreadConversation(replyToNoteId: String) {
        viewModelScope.launch {
            feedRepository.observeConversation(userId = activeAccountStore.activeUserId(), noteId = replyToNoteId)
                .filter { it.isNotEmpty() }
                .map { posts -> posts.map { it.asFeedPostUi() } }
                .collect { conversation ->
                    val replyToNoteIndex = conversation.indexOfFirst { it.postId == replyToNoteId }
                    val thread = conversation.subList(0, replyToNoteIndex + 1)
                    setState { copy(replyToConversation = thread) }
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
                feedRepository.fetchReplies(userId = activeAccountStore.activeUserId(), noteId = replyToNoteId)
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun fetchAndUpdateNoteUriDetails(uri: String, nevent: Nevent) =
        viewModelScope.launch {
            setState {
                copy(
                    referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Note>(
                        uri = uri,
                    ) { copy(loading = true) },
                )
            }
            fetchAndGet<FeedPost, NetworkException>(
                fetch = {
                    feedRepository.fetchConversation(
                        userId = activeAccountStore.activeUserId(),
                        noteId = nevent.eventId,
                        limit = 1,
                    )
                },
                get = { feedRepository.findAllPostsByIds(postIds = listOf(nevent.eventId)).firstOrNull() },
                onFinally = {
                    setState {
                        copy(
                            referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Note>(
                                uri = uri,
                            ) { copy(loading = false) },
                        )
                    }
                },
            ) { post ->
                setState {
                    copy(
                        referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Note>(uri = uri) {
                            copy(data = post.asFeedPostUi())
                        },
                    )
                }
            }
        }

    private fun getAndUpdateHighlightUriDetails(uri: String, nevent: Nevent) =
        viewModelScope.launch {
            setState {
                copy(
                    referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Highlight>(
                        uri = uri,
                    ) { copy(loading = true) },
                )
            }

            val highlight = highlightRepository.getHighlightById(highlightId = nevent.eventId)

            setState {
                copy(
                    referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Highlight>(uri = uri) {
                        copy(data = highlight?.asHighlightUi(), loading = false)
                    },
                )
            }
        }

    private fun fetchAndUpdateArticleUriDetails(uri: String, naddr: Naddr) =
        viewModelScope.launch {
            setState {
                copy(
                    referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Article>(
                        uri = uri,
                    ) { copy(loading = true) },
                )
            }

            fetchAndGet<Article, NetworkException>(
                fetch = {
                    articleRepository.fetchArticleAndComments(
                        userId = activeAccountStore.activeUserId(),
                        articleId = naddr.identifier,
                        articleAuthorId = naddr.userId,
                    )
                },
                get = { articleRepository.getArticleByATag(aTag = naddr.asATagValue()) },
                onFinally = {
                    setState {
                        copy(
                            referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Article>(uri = uri) {
                                copy(loading = false)
                            },
                        )
                    }
                },
            ) { article ->
                setState {
                    copy(
                        referencedNostrUris = referencedNostrUris.updateByUri<ReferencedUri.Article>(uri = uri) {
                            copy(data = article.mapAsFeedArticleUi())
                        },
                    )
                }
            }
        }

    private fun fetchArticleDetailsFromNetwork(replyToArticleNaddr: Naddr) =
        viewModelScope.launch {
            try {
                articleRepository.fetchArticleAndComments(
                    userId = activeAccountStore.activeUserId(),
                    articleId = replyToArticleNaddr.identifier,
                    articleAuthorId = replyToArticleNaddr.userId,
                )
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun publishPost() =
        viewModelScope.launch {
            setState { copy(publishing = true) }
            try {
                val noteContent = _state.value.content.text
                    .replaceUserMentionsWithUserIds(users = _state.value.taggedUsers)

                val publishResult = if (args.isQuoting) {
                    notePublishHandler.publishShortTextNote(
                        userId = activeAccountStore.activeUserId(),
                        content = noteContent.concatenateUris(),
                        attachments = _state.value.attachments,
                    )
                } else {
                    val rootPost = _state.value.replyToConversation.firstOrNull()
                    val replyToPost = _state.value.replyToConversation.lastOrNull()
                    notePublishHandler.publishShortTextNote(
                        userId = activeAccountStore.activeUserId(),
                        content = noteContent.concatenateUris(),
                        attachments = _state.value.attachments,
                        rootNoteNevent = rootPost?.asNevent(),
                        replyToNoteNevent = replyToPost?.asNevent(),
                        rootArticleNaddr = referencedArticleNaddr
                            ?: _state.value.replyToArticle?.generateNaddr(),
                        rootHighlightNevent = referencedHighlightNevent
                            ?: _state.value.replyToHighlight?.generateNevent(),
                    )
                }

                if (referencedNoteNevent != null) {
                    if (publishResult.imported) {
                        fetchNoteReplies()
                    } else {
                        scheduleFetchReplies()
                    }
                }

                resetState()

                sendEffect(SideEffect.PostPublished)
            } catch (error: SignatureException) {
                Timber.w(error)
                setErrorState(error = UiError.PublishError(cause = error.cause))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiError.PublishError(cause = error.cause))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiError.MissingRelaysConfiguration(cause = error.cause))
            } finally {
                setState { copy(publishing = false) }
            }
        }

    private fun fetchNoteReplies() {
        if (referencedNoteNevent != null) {
            fetchNoteThreadFromNetwork(referencedNoteNevent.eventId)
        }
    }

    private fun scheduleFetchReplies() =
        viewModelScope.launch {
            delay(750.milliseconds)
            fetchNoteReplies()
        }

    private suspend fun String.replaceUserMentionsWithUserIds(users: List<NoteTaggedUser>): String {
        var content = this
        val userRelaysMap = try {
            relayRepository
                .fetchAndUpdateUserRelays(userIds = users.map { it.userId })
                .associateBy { it.pubkey }
        } catch (error: NetworkException) {
            Timber.w(error)
            emptyMap()
        }

        users.forEach { user ->
            val nprofile = Nprofile(
                pubkey = user.userId,
                relays = userRelaysMap[user.userId]?.relays
                    ?.filter { it.write }?.map { it.url }?.take(MAX_RELAY_HINTS) ?: emptyList(),
            )
            content = content.replace(
                oldValue = user.displayUsername,
                newValue = "nostr:${nprofile.toNprofileString()}",
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
                    val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
                        uploadAttachment(attachment = it)
                    }
                    val uploadJob = UploadJob(job = job)
                    attachmentUploads[it.id] = uploadJob
                    uploadJob
                }.forEach {
                    it.job.start()
                    it.job.join()
                }
            checkUploadQueueAndDisableFlagIfCompleted()
        }
    }

    private suspend fun uploadAttachment(attachment: NoteAttachment) {
        var updatedAttachment = attachment
        try {
            setState { copy(uploadingAttachments = true) }
            updatedAttachment = updatedAttachment.copy(uploadError = null)
            updateNoteAttachmentState(attachment = updatedAttachment)

            val uploadResult = primalUploadService.upload(
                uri = attachment.localUri,
                userId = activeAccountStore.activeUserId(),
                onProgress = { uploadedBytes, totalBytes ->
                    updatedAttachment = updatedAttachment.copy(
                        originalUploadedInBytes = uploadedBytes,
                        originalSizeInBytes = totalBytes,
                    )
                    updateNoteAttachmentState(attachment = updatedAttachment)
                },
            )

            when (uploadResult) {
                is UploadResult.Success -> {
                    updatedAttachment = updatedAttachment.copy(
                        remoteUrl = uploadResult.remoteUrl,
                        originalHash = uploadResult.originalHash,
                        originalSizeInBytes = uploadResult.originalFileSize.toInt(),
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
                }

                is UploadResult.Failed -> {
                    Timber.w(uploadResult.error)
                    updateNoteAttachmentState(attachment = updatedAttachment.copy(uploadError = uploadResult.error))
                    setErrorState(error = UiError.FailedToUploadAttachment(cause = uploadResult.error))
                }
            }
        } catch (error: SignatureException) {
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

    private fun UploadJob?.cancel() = this?.job?.cancel()

    private fun retryAttachmentUpload(attachmentId: UUID) =
        viewModelScope.launch {
            val noteAttachment = _state.value.attachments.firstOrNull { it.id == attachmentId }
            if (noteAttachment != null) {
                val job = viewModelScope.launch {
                    uploadAttachment(attachment = noteAttachment)
                }
                attachmentUploads[attachmentId] = UploadJob(job = job)
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

    private fun setErrorState(error: UiError) {
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
            userRepository.observeRecentUsers(ownerId = activeAccountStore.activeUserId())
                .distinctUntilChanged()
                .collect {
                    setState { copy(recentUsers = it.map { it.mapAsUserProfileUi() }) }
                }
        }
    }

    private fun fetchPopularUsers() =
        viewModelScope.launch {
            try {
                val popularUsers = exploreRepository.fetchPopularUsers()
                setState { copy(popularUsers = popularUsers.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun searchUserTagging(query: String) =
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                try {
                    val result = exploreRepository.searchUsers(query = query, limit = 10)
                    setState { copy(users = result.map { it.mapAsUserProfileUi() }) }
                } catch (error: NetworkException) {
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
            userRepository.markAsInteracted(profileId = profileId, ownerId = activeAccountStore.activeUserId())
        }
    }

    private inline fun <reified T : ReferencedUri<*>> List<ReferencedUri<*>>.updateByUri(
        uri: String,
        reducer: T.() -> T,
    ): List<ReferencedUri<*>> =
        map {
            if (it.uri == uri && it is T) {
                it.reducer()
            } else {
                it
            }
        }

    private fun mapUriToReferencedUri(uri: String): ReferencedUri<*>? {
        if (uri.isLnInvoice()) {
            return ReferencedUri.LightningInvoice(
                data = uri,
                uri = uri,
                loading = false,
            )
        }

        return uri.takeAsNaddrOrNull()?.let { naddr ->
            ReferencedUri.Article(
                data = null,
                loading = true,
                uri = uri,
                naddr = naddr,
            )
        } ?: uri.takeAsNeventOrNull()
            .takeIf {
                it?.kind == NostrEventKind.ShortTextNote.value ||
                    it?.kind == NostrEventKind.Highlight.value
            }
            ?.let { nevent ->
                when (nevent.kind) {
                    NostrEventKind.ShortTextNote.value ->
                        ReferencedUri.Note(
                            data = null,
                            loading = true,
                            uri = uri,
                            nevent = nevent,
                        )

                    NostrEventKind.Highlight.value ->
                        ReferencedUri.Highlight(
                            data = null,
                            loading = true,
                            uri = uri,
                            nevent = nevent,
                        )

                    else -> null
                }
            }
    }

    private suspend fun FeedPostUi.asNevent(): Nevent {
        val relayHints = runCatching { relayHintsRepository.findRelaysByIds(listOf(this.postId)) }.getOrNull()

        return Nevent(
            kind = NostrEventKind.ShortTextNote.value,
            userId = this.authorId,
            eventId = this.postId,
            relays = relayHints?.firstOrNull { it.eventId == this.postId }?.relays?.take(MAX_RELAY_HINTS)
                ?: emptyList(),
        )
    }

    private fun String.concatenateUris(): String {
        return this + state.value.referencedNostrUris.map { it.uri }
            .joinToString(separator = " \n\n", prefix = " \n\n") { it.withNostrPrefix() }
    }
}
