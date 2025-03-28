package net.primal.android.thread.articles.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.highlights.model.JoinedHighlightsUi
import net.primal.android.highlights.model.joinOnContent
import net.primal.android.navigation.articleId
import net.primal.android.navigation.naddr
import net.primal.android.navigation.primalName
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.ext.isNPubUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.isNoteUri
import net.primal.android.nostr.ext.nostrUriToNoteId
import net.primal.android.nostr.ext.nostrUriToPubkey
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.nostr.notary.exceptions.NostrSignUnauthorized
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.thread.articles.details.ArticleDetailsContract.SideEffect
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiState
import net.primal.android.thread.articles.details.ui.mapAsArticleDetailsUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.repository.ArticleRepository
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.HighlightRepository
import net.primal.domain.repository.ProfileRepository
import timber.log.Timber

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
    private val highlightRepository: HighlightRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val eventInteractionRepository: EventInteractionRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
        resolveNaddr()
    }

    private fun resolveNaddr() =
        viewModelScope.launch {
            setState { copy(isResolvingNaddr = true, error = null) }
            val naddr = parseAndResolveNaddr()

            if (naddr == null) {
                setState { copy(error = UiError.InvalidNaddr) }
            } else {
                setState { copy(naddr = naddr) }

                observeArticle(naddr)
                observeArticleComments(naddr = naddr)
            }

            setState { copy(isResolvingNaddr = false) }
        }

    @Suppress("CyclomaticComplexMethod")
    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> fetchData()
                    UiEvent.DismissErrors -> dismissErrors()
                    is UiEvent.ZapArticle -> zapArticle(zapAction = it)
                    UiEvent.LikeArticle -> likeArticle()
                    UiEvent.RepostAction -> repostPost()
                    UiEvent.ToggleAuthorFollows -> followUnfollowAuthor()
                    UiEvent.ToggleHighlights -> toggleHighlightsVisibility()
                    is UiEvent.PublishHighlight -> publishNewHighlight(it)
                    is UiEvent.SelectHighlight -> selectHighlight(content = it.content)
                    UiEvent.DismissSelectedHighlight -> dismissSelectedHighlight()
                    UiEvent.DeleteSelectedHighlight -> _state.value.selectedHighlight?.let { selectedHighlight ->
                        removeSelectedHighlight(selectedHighlight)
                    }

                    UiEvent.PublishSelectedHighlight -> _state.value.selectedHighlight?.let { selectedHighlight ->
                        publishAndSaveHighlight(
                            content = selectedHighlight.content,
                            context = selectedHighlight.context,
                            articleATag = selectedHighlight.referencedEventATag,
                            articleAuthorId = selectedHighlight.referencedEventAuthorId,
                        )
                    }

                    UiEvent.RequestResolveNaddr -> resolveNaddr()
                }
            }
        }

    private fun fetchData() =
        viewModelScope.launch {
            state.value.naddr?.let { naddr ->
                try {
                    articleRepository.fetchArticleAndComments(
                        userId = activeAccountStore.activeUserId(),
                        articleAuthorId = naddr.userId,
                        articleId = naddr.identifier,
                    )
                    articleRepository.fetchArticleHighlights(
                        userId = activeAccountStore.activeUserId(),
                        articleAuthorId = naddr.userId,
                        articleId = naddr.identifier,
                    )
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }

    private fun observeArticle(naddr: Naddr) =
        viewModelScope.launch {
            var referencedNotesUris: Set<String> = emptySet()
            var referencedProfileUris: Set<String> = emptySet()
            articleRepository.observeArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
                .collect { article ->
                    val nostrNoteUris = article.uris.filter { it.isNoteUri() || it.isNote() }.toSet()
                    if (nostrNoteUris != referencedNotesUris) {
                        referencedNotesUris = nostrNoteUris
                        val referencedNotes = feedRepository.findAllPostsByIds(
                            postIds = nostrNoteUris.mapNotNull { it.nostrUriToNoteId() },
                        )
                        setState {
                            copy(
                                referencedNotes = referencedNotes.map { it.asFeedPostUi() },
                            )
                        }
                    }

                    val nostrProfileUris = article.uris.filter { it.isNPubUri() || it.isNPub() }.toSet()
                    if (nostrProfileUris != referencedProfileUris) {
                        referencedProfileUris = nostrProfileUris
                        val referencedProfiles = profileRepository.findProfileData(
                            profileIds = nostrProfileUris.mapNotNull { it.nostrUriToPubkey() },
                        )
                        setState {
                            copy(
                                npubToDisplayNameMap = referencedProfiles
                                    .associateBy { it.profileId.hexToNpubHrp() }
                                    .mapValues { "@${it.value.authorNameUiFriendly()}" },
                            )
                        }
                    }

                    setState {
                        val joinedHighlights = article.highlights.joinOnContent()
                        val selectedHighlight = selectedHighlight?.let {
                            joinedHighlights.firstOrNull { it.content == selectedHighlight.content }
                        }
                        copy(
                            article = article.mapAsArticleDetailsUi(),
                            highlights = joinedHighlights,
                            selectedHighlight = selectedHighlight,
                            topZaps = article.eventZaps
                                .map { it.asEventZapUiModel() }
                                .sortedWith(EventZapUiModel.DefaultComparator),
                        )
                    }
                }
        }

    private fun observeArticleComments(naddr: Naddr) =
        viewModelScope.launch {
            articleRepository.observeArticleComments(
                userId = activeAccountStore.activeUserId(),
                articleId = naddr.identifier,
                articleAuthorId = naddr.userId,
            ).collect { comments ->
                setState { copy(comments = comments.map { it.asFeedPostUi() }) }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState
                .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
                .collect {
                    setState {
                        copy(
                            isAuthorFollowed = it.data.following.contains(naddr?.userId),
                            zappingState = this.zappingState.copy(
                                walletConnected = it.data.hasWallet(),
                                walletPreference = it.data.walletPreference,
                                zapDefault = it.data.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                                zapsConfig = it.data.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                                walletBalanceInBtc = it.data.primalWalletState.balanceInBtc,
                            ),
                        )
                    }
                }
        }

    private fun zapArticle(zapAction: UiEvent.ZapArticle) {
        val article = _state.value.article ?: return

        viewModelScope.launch {
            val postAuthorProfileData = profileRepository.findProfileDataOrNull(profileId = article.authorId)
            val lnUrlDecoded = postAuthorProfileData?.lnUrlDecoded
            if (lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.ReplaceableEvent(
                        kind = NostrEventKind.LongFormContent.value,
                        identifier = article.articleId,
                        eventId = article.eventId,
                        eventAuthorId = article.authorId,
                        eventAuthorLnUrlDecoded = lnUrlDecoded,
                    ),
                )
            } catch (error: ZapFailureException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setState { copy(error = UiError.InvalidZapRequest(error)) }
            }
        }
    }

    private fun likeArticle() =
        viewModelScope.launch {
            val article = _state.value.article
            if (article != null) {
                try {
                    eventInteractionRepository.likeEvent(
                        userId = activeAccountStore.activeUserId(),
                        eventId = article.eventId,
                        eventAuthorId = article.authorId,
                        optionalTags = listOf(
                            "${NostrEventKind.LongFormContent.value}:${article.authorId}:${article.articleId}"
                                .asReplaceableEventTag(),
                        ),
                    )
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                } catch (error: MissingRelaysException) {
                    Timber.w(error)
                } catch (error: MissingPrivateKey) {
                    setState { copy(error = UiError.MissingPrivateKey) }
                    Timber.w(error)
                } catch (error: NostrSignUnauthorized) {
                    setState { copy(error = UiError.NostrSignUnauthorized) }
                    Timber.w(error)
                }
            }
        }

    private fun repostPost() =
        viewModelScope.launch {
            val article = _state.value.article
            if (article != null) {
                try {
                    eventInteractionRepository.repostEvent(
                        userId = activeAccountStore.activeUserId(),
                        eventId = article.eventId,
                        eventAuthorId = article.authorId,
                        eventKind = NostrEventKind.LongFormContent.value,
                        eventRawNostrEvent = article.eventRawNostrEvent,
                        optionalTags = listOf(
                            "${NostrEventKind.LongFormContent.value}:${article.authorId}:${article.articleId}"
                                .asReplaceableEventTag(),
                        ),
                    )
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                } catch (error: MissingRelaysException) {
                    Timber.w(error)
                } catch (error: MissingPrivateKey) {
                    setState { copy(error = UiError.MissingPrivateKey) }
                    Timber.w(error)
                } catch (error: NostrSignUnauthorized) {
                    setState { copy(error = UiError.NostrSignUnauthorized) }
                    Timber.w(error)
                }
            }
        }

    private fun followUnfollowAuthor() {
        val article = _state.value.article ?: return
        val isAuthorFollowed = _state.value.isAuthorFollowed
        setState { copy(isAuthorFollowed = !isAuthorFollowed) }

        viewModelScope.launch {
            val followUnfollowResult = runCatching {
                if (isAuthorFollowed) {
                    userRepository.unfollow(
                        userId = activeAccountStore.activeUserId(),
                        unfollowedUserId = article.authorId,
                        forceUpdate = false,
                    )
                } else {
                    userRepository.follow(
                        userId = activeAccountStore.activeUserId(),
                        followedUserId = article.authorId,
                        forceUpdate = false,
                    )
                }
            }

            if (followUnfollowResult.isFailure) {
                followUnfollowResult.exceptionOrNull()?.let { error ->
                    when (error) {
                        is WssException, is UserRepository.FollowListNotFound,
                        is NostrPublishException,
                        -> {
                            Timber.e(error)
                            setState {
                                copy(
                                    isAuthorFollowed = isAuthorFollowed,
                                    error = if (isAuthorFollowed) {
                                        UiError.FailedToUnfollowUser(cause = error)
                                    } else {
                                        UiError.FailedToFollowUser(cause = error)
                                    },
                                )
                            }
                        }

                        is NostrSignUnauthorized -> {
                            Timber.e(error)
                            setState {
                                copy(
                                    isAuthorFollowed = isAuthorFollowed,
                                    error = UiError.NostrSignUnauthorized,
                                )
                            }
                        }

                        is MissingPrivateKey -> {
                            Timber.e(error)
                            setState { copy(isAuthorFollowed = isAuthorFollowed, error = UiError.MissingPrivateKey) }
                        }

                        else -> throw error
                    }
                }
            }
        }
    }

    private fun publishNewHighlight(event: UiEvent.PublishHighlight) {
        val naddr = state.value.naddr

        publishAndSaveHighlight(
            content = event.content,
            context = event.context,
            articleATag = naddr?.asATagValue(),
            articleAuthorId = naddr?.userId,
            onSuccess = { highlightNevent ->
                naddr?.let { articleNaddr ->
                    setEffect(
                        SideEffect.HighlightCreated(
                            articleNaddr = articleNaddr,
                            highlightNevent = highlightNevent,
                            isCommentRequested = event.isCommentRequested,
                            isQuoteRequested = event.isQuoteRequested,
                        ),
                    )
                }
            },
        )
    }

    private fun publishAndSaveHighlight(
        content: String,
        context: String?,
        articleATag: String?,
        articleAuthorId: String?,
        onSuccess: ((highlightNevent: Nevent) -> Unit)? = null,
    ) = viewModelScope.launch {
        setState { copy(isWorking = true) }
        try {
            val highlightNevent = highlightRepository.publishAndSaveHighlight(
                userId = activeAccountStore.activeUserId(),
                content = content,
                referencedEventATag = articleATag,
                referencedEventAuthorTag = articleAuthorId,
                context = context,
            )
            setState { copy(isHighlighted = true) }
            onSuccess?.invoke(highlightNevent)
        } catch (error: MissingPrivateKey) {
            setState { copy(error = UiError.MissingPrivateKey) }
            Timber.w(error)
        } catch (error: NostrSignUnauthorized) {
            setState { copy(error = UiError.NostrSignUnauthorized) }
            Timber.w(error)
        } catch (error: NostrPublishException) {
            Timber.w(error)
        } finally {
            setState { copy(isWorking = false) }
        }
    }

    private fun removeSelectedHighlight(selectedHighlight: JoinedHighlightsUi) =
        viewModelScope.launch {
            val rawHighlights = _state.value.article?.highlights

            val highlightToDelete = rawHighlights?.find {
                it.content == selectedHighlight.content &&
                    it.referencedEventATag == selectedHighlight.referencedEventATag &&
                    it.author?.pubkey == activeAccountStore.activeUserId()
            }
            if (highlightToDelete == null) {
                Timber.w("We are trying to remove a highlight that doesn't exist.")
                return@launch
            }

            setState { copy(isWorking = true) }
            try {
                highlightRepository.publishDeleteHighlight(
                    userId = activeAccountStore.activeUserId(),
                    highlightId = highlightToDelete.highlightId,
                )

                setState { copy(isHighlighted = false) }
            } catch (error: MissingPrivateKey) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: NostrSignUnauthorized) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            } catch (error: NostrPublishException) {
                Timber.w(error)
            } finally {
                setState { copy(isWorking = false) }
            }
        }

    private fun selectHighlight(content: String) {
        setState {
            val highlight = highlights.first { h -> h.content == content }
            copy(
                selectedHighlight = highlight,
                isHighlighted = highlight.authors.map { a -> a.pubkey }
                    .contains(activeAccountStore.activeUserId()),
            )
        }
    }

    private fun dismissSelectedHighlight() {
        setState {
            copy(
                selectedHighlight = null,
                isHighlighted = false,
            )
        }
    }

    private suspend fun parseAndResolveNaddr() =
        savedStateHandle.naddr?.let { Nip19TLV.parseUriAsNaddrOrNull(it) }
            ?: run {
                val identifier = savedStateHandle.articleId
                val userId = savedStateHandle.primalName?.let {
                    runCatching { profileRepository.fetchProfileId(it) }.getOrNull()
                }

                if (identifier != null && userId != null) {
                    Naddr(
                        identifier = identifier,
                        userId = userId,
                        kind = NostrEventKind.LongFormContent.value,
                    )
                } else {
                    null
                }
            }

    private fun toggleHighlightsVisibility() {
        setState { copy(showHighlights = !showHighlights) }
    }

    private fun dismissErrors() {
        setState { copy(error = null) }
    }
}
