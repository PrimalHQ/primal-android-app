package net.primal.android.thread.articles.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticleRepository
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.navigation.naddrOrThrow
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.ext.isNPubUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.isNoteUri
import net.primal.android.nostr.ext.nostrUriToNoteId
import net.primal.android.nostr.ext.nostrUriToPubkey
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.stats.repository.EventRepository
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.stats.ui.asEventZapUiModel
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiState
import net.primal.android.thread.articles.details.ui.mapAsArticleDetailsUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
    private val eventRepository: EventRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val naddr = Nip19TLV.parseUriAsNaddrOrNull(savedStateHandle.naddrOrThrow)

    private val _state = MutableStateFlow(UiState(naddr = naddr))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()

        if (naddr == null) {
            setState { copy(error = UiError.InvalidNaddr) }
        } else {
            observeArticle(naddr)
            observeArticleComments(naddr = naddr)
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> fetchData(naddr)
                    UiEvent.DismissErrors -> setState { copy(error = null) }
                    is UiEvent.ZapArticle -> zapArticle(zapAction = it)
                    UiEvent.LikeArticle -> likeArticle()
                    UiEvent.RepostAction -> repostPost()
                    UiEvent.ToggleAuthorFollows -> followUnfollowAuthor()
                    is UiEvent.SelectHighlight -> setState {
                        copy(selectedHighlight = article?.highlights?.first { h -> h.content == it.content })
                    }

                    UiEvent.DismissSelectedHighlight -> setState { copy(selectedHighlight = null) }
                }
            }
        }

    private fun fetchData(naddr: Naddr?) =
        viewModelScope.launch {
            if (naddr != null) {
                try {
                    articleRepository.fetchArticleAndComments(
                        articleAuthorId = naddr.userId,
                        articleId = naddr.identifier,
                    )
                    articleRepository.fetchArticleHighlights(
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
                    val nostrNoteUris = article.data.uris.filter { it.isNoteUri() || it.isNote() }.toSet()
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

                    val nostrProfileUris = article.data.uris.filter { it.isNPubUri() || it.isNPub() }.toSet()
                    if (nostrProfileUris != referencedProfileUris) {
                        referencedProfileUris = nostrProfileUris
                        val referencedProfiles = profileRepository.findProfilesData(
                            profileIds = nostrProfileUris.mapNotNull { it.nostrUriToPubkey() },
                        )
                        setState {
                            copy(
                                npubToDisplayNameMap = referencedProfiles
                                    .associateBy { it.ownerId.hexToNpubHrp() }
                                    .mapValues { "@${it.value.authorNameUiFriendly()}" },
                            )
                        }
                    }

                    setState {
                        copy(
                            article = article.mapAsArticleDetailsUi(),
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
            if (postAuthorProfileData?.lnUrlDecoded == null) {
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
                        eventAuthorLnUrlDecoded = postAuthorProfileData.lnUrlDecoded,
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
                    eventRepository.likeEvent(
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
                }
            }
        }

    private fun repostPost() =
        viewModelScope.launch {
            val article = _state.value.article
            if (article != null) {
                try {
                    eventRepository.repostEvent(
                        userId = activeAccountStore.activeUserId(),
                        eventId = article.eventId,
                        eventAuthorId = article.authorId,
                        eventKind = NostrEventKind.LongFormContent,
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
                    profileRepository.unfollow(
                        userId = activeAccountStore.activeUserId(),
                        unfollowedUserId = article.authorId,
                        forceUpdate = false,
                    )
                } else {
                    profileRepository.follow(
                        userId = activeAccountStore.activeUserId(),
                        followedUserId = article.authorId,
                        forceUpdate = false,
                    )
                }
            }

            if (followUnfollowResult.isFailure) {
                followUnfollowResult.exceptionOrNull()?.let { error ->
                    when (error) {
                        is WssException, is ProfileRepository.FollowListNotFound, is NostrPublishException -> {
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

                        else -> throw error
                    }
                }
            }
        }
    }
}
