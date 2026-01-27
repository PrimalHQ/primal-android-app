package net.primal.android.thread.articles.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.articles.highlights.JoinedHighlightsUi
import net.primal.android.articles.highlights.joinOnContent
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.navigation.articleId
import net.primal.android.navigation.articleNaddr
import net.primal.android.navigation.primalName
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.thread.articles.details.ArticleDetailsContract.SideEffect
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiState
import net.primal.android.thread.articles.details.ui.mapAsArticleDetailsUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.isNEvent
import net.primal.domain.nostr.utils.isNEventUri
import net.primal.domain.nostr.utils.isNProfile
import net.primal.domain.nostr.utils.isNProfileUri
import net.primal.domain.nostr.utils.isNPub
import net.primal.domain.nostr.utils.isNPubUri
import net.primal.domain.nostr.utils.isNote
import net.primal.domain.nostr.utils.isNoteUri
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.posts.FeedRepository
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.reads.ArticleRepository
import net.primal.domain.reads.HighlightRepository
import net.primal.domain.utils.isConfigured

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
    private val highlightRepository: HighlightRepository,
    private val profileRepository: ProfileRepository,
    private val profileFollowsHandler: ProfileFollowsHandler,
    private val eventInteractionRepository: EventInteractionRepository,
    private val zapHandler: ZapHandler,
    private val walletAccountRepository: WalletAccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(activeAccountUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        observeEvents()
        observeFollowsResults()
        resolveNaddrAndInit()
    }

    private fun resolveNaddrAndInit() =
        viewModelScope.launch {
            setState { copy(isResolvingNaddr = true, error = null) }
            val naddr = parseAndResolveNaddr()

            if (naddr == null) {
                setState { copy(error = UiError.InvalidNaddr) }
            } else {
                setState { copy(naddr = naddr) }

                fetchData(naddr)
                observeArticle(naddr)
                observeArticleComments(naddr = naddr)
                observeActiveWallet()
                observeActiveAccount()
            }

            setState { copy(isResolvingNaddr = false) }
        }

    @Suppress("CyclomaticComplexMethod")
    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> state.value.naddr?.let { naddr -> fetchData(naddr) }
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

                    UiEvent.RequestResolveNaddr -> resolveNaddrAndInit()
                }
            }
        }

    private fun fetchData(naddr: Naddr) =
        viewModelScope.launch {
            try {
                setState { copy(fetching = true) }
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
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to fetch article data." }
            } finally {
                setState { copy(fetching = false) }
            }
        }

    private fun observeArticle(naddr: Naddr) =
        viewModelScope.launch {
            var referencedNotesUris: Set<String> = emptySet()
            var referencedProfileUris: Set<String> = emptySet()
            articleRepository.observeArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
                .collect { article ->
                    val nostrNoteUris = article.uris
                        .filter { it.isNoteUri() || it.isNote() || it.isNEventUri() || it.isNEvent() }
                        .toSet()

                    if (nostrNoteUris != referencedNotesUris) {
                        referencedNotesUris = nostrNoteUris
                        val referencedNotes = feedRepository.findAllPostsByIds(
                            postIds = nostrNoteUris.mapNotNull { it.extractNoteId() },
                        )
                        setState {
                            copy(
                                referencedNotes = referencedNotes.map { it.asFeedPostUi() },
                            )
                        }
                    }

                    val nostrProfileUris = article.uris
                        .filter { it.isNPubUri() || it.isNPub() || it.isNProfileUri() || it.isNProfile() }
                        .toSet()
                    if (nostrProfileUris != referencedProfileUris) {
                        referencedProfileUris = nostrProfileUris
                        val referencedProfiles = profileRepository.findProfileData(
                            profileIds = nostrProfileUris.mapNotNull { it.extractProfileId() },
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

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    setState {
                        copy(
                            zappingState = zappingState.copy(
                                walletConnected = wallet.isConfigured(),
                                walletBalanceInBtc = wallet?.balanceInBtc?.formatAsString(),
                            ),
                        )
                    }
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState
                .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
                .collect {
                    setState {
                        copy(
                            activeAccountUserId = activeAccountStore.activeUserId(),
                            isAuthorFollowed = it.data.following.contains(naddr?.userId),
                            zappingState = this.zappingState.copy(
                                zapDefault = it.data.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                                zapsConfig = it.data.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
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

            val walletId = walletAccountRepository.getActiveWallet(userId = activeAccountStore.activeUserId())?.walletId
                ?: return@launch

            val result = zapHandler.zap(
                userId = activeAccountStore.activeUserId(),
                walletId = walletId,
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.ReplaceableEvent(
                    aTag = article.aTag,
                    eventId = article.eventId,
                    recipientUserId = article.authorId,
                    recipientLnUrlDecoded = lnUrlDecoded,
                ),
            )

            if (result is ZapResult.Failure) {
                when (result.error) {
                    is ZapError.InvalidZap, is ZapError.FailedToFetchZapPayRequest,
                    is ZapError.FailedToFetchZapInvoice,
                    -> setState { copy(error = UiError.InvalidZapRequest()) }

                    is ZapError.FailedToPayZap, ZapError.FailedToPublishEvent, ZapError.FailedToSignEvent,
                    is ZapError.Timeout,
                    -> {
                        setState { copy(error = UiError.FailedToPublishZapEvent()) }
                    }

                    is ZapError.Unknown -> {
                        setState { copy(error = UiError.GenericError()) }
                    }
                }
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
                    Napier.w(throwable = error) { "Failed to publish like event." }
                } catch (error: MissingRelaysException) {
                    Napier.w(throwable = error) { "Missing relays for like event." }
                } catch (error: SigningKeyNotFoundException) {
                    setState { copy(error = UiError.MissingPrivateKey) }
                    Napier.w(throwable = error) { "Signing key not found for like event." }
                } catch (error: SigningRejectedException) {
                    setState { copy(error = UiError.NostrSignUnauthorized) }
                    Napier.w(throwable = error) { "Signing rejected for like event." }
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
                    Napier.w(throwable = error) { "Failed to publish repost event." }
                } catch (error: MissingRelaysException) {
                    Napier.w(throwable = error) { "Missing relays for repost event." }
                } catch (error: SigningKeyNotFoundException) {
                    setState { copy(error = UiError.MissingPrivateKey) }
                    Napier.w(throwable = error) { "Signing key not found for repost event." }
                } catch (error: SigningRejectedException) {
                    setState { copy(error = UiError.NostrSignUnauthorized) }
                    Napier.w(throwable = error) { "Signing rejected for repost event." }
                }
            }
        }

    private fun followUnfollowAuthor() {
        val article = _state.value.article ?: return
        val isAuthorFollowed = _state.value.isAuthorFollowed
        setState { copy(isAuthorFollowed = !isAuthorFollowed) }

        viewModelScope.launch {
            if (isAuthorFollowed) {
                profileFollowsHandler.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    profileId = article.authorId,
                )
            } else {
                profileFollowsHandler.follow(
                    userId = activeAccountStore.activeUserId(),
                    profileId = article.authorId,
                )
            }
        }
    }

    private fun observeFollowsResults() =
        viewModelScope.launch {
            profileFollowsHandler.observeResults().collect {
                when (it) {
                    is ProfileFollowsHandler.ActionResult.Error -> {
                        Napier.e(throwable = it.error) { "Failed to update follow list." }
                        setState { copy(isAuthorFollowed = !isAuthorFollowed) }

                        when (it.error) {
                            is NetworkException, is UserRepository.FollowListNotFound,
                            is NostrPublishException,
                            -> setState { copy(error = UiError.FailedToUpdateFollowList(it.error)) }

                            is SigningRejectedException -> setState { copy(error = UiError.NostrSignUnauthorized) }

                            is SigningKeyNotFoundException -> setState { copy(error = UiError.MissingPrivateKey) }
                        }
                    }

                    ProfileFollowsHandler.ActionResult.Success -> Unit
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
        } catch (error: SigningKeyNotFoundException) {
            setState { copy(error = UiError.MissingPrivateKey) }
            Napier.w(throwable = error) { "Signing key not found for publishing highlight." }
        } catch (error: SigningRejectedException) {
            setState { copy(error = UiError.NostrSignUnauthorized) }
            Napier.w(throwable = error) { "Signing rejected for publishing highlight." }
        } catch (error: NostrPublishException) {
            Napier.w(throwable = error) { "Failed to publish highlight." }
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
                Napier.w { "We are trying to remove a highlight that doesn't exist." }
                return@launch
            }

            setState { copy(isWorking = true) }
            try {
                highlightRepository.publishDeleteHighlight(
                    userId = activeAccountStore.activeUserId(),
                    highlightId = highlightToDelete.highlightId,
                )

                setState { copy(isHighlighted = false) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Napier.w(throwable = error) { "Signing key not found for deleting highlight." }
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Napier.w(throwable = error) { "Signing rejected for deleting highlight." }
            } catch (error: NostrPublishException) {
                Napier.w(throwable = error) { "Failed to publish delete highlight event." }
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
        savedStateHandle.articleNaddr?.let { Nip19TLV.parseUriAsNaddrOrNull(it) }
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
