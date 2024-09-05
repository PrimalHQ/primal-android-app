package net.primal.android.thread.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticleRepository
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.navigation.naddrOrThrow
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.ext.isNPubUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.isNoteUri
import net.primal.android.nostr.ext.nostrUriToNoteId
import net.primal.android.nostr.ext.nostrUriToPubkey
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.note.repository.NoteRepository
import net.primal.android.note.ui.asEventZapUiModel
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.thread.articles.ArticleDetailsContract.ArticleDetailsError
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ArticleDetailsContract.UiState
import net.primal.android.thread.articles.ui.mapAsArticleDetailsUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val naddr = Nip19TLV.parseAsNaddr(savedStateHandle.naddrOrThrow)

    private val _state = MutableStateFlow(UiState(naddr = naddr))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var topZappersObserveJob: Job? = null

    init {
        observeEvents()
        observeActiveAccount()

        if (naddr == null) {
            setState { copy(error = ArticleDetailsError.InvalidNaddr) }
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
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }

    private fun observeArticle(naddr: Naddr) =
        viewModelScope.launch {
            var eventId: String? = null
            var referencedNotesUris: Set<String> = emptySet()
            var referencedProfileUris: Set<String> = emptySet()
            articleRepository.observeArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
                .collect { article ->
                    if (article.data.eventId != eventId) {
                        eventId = article.data.eventId
                        observeTopZappers(eventId = article.data.eventId)
                    }

                    val nostrNoteUris = article.data.uris.filter { it.isNoteUri() || it.isNote() }.toSet()
                    if (nostrNoteUris != referencedNotesUris) {
                        referencedNotesUris = nostrNoteUris
                        val referencedNotes = feedRepository.findAllPostsByIds(
                            postIds = nostrNoteUris.mapNotNull { it.nostrUriToNoteId() },
                        )
                        setState { copy(referencedNotes = referencedNotes.map { it.asFeedPostUi() }) }
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

                    setState { copy(article = article.mapAsArticleDetailsUi()) }
                }
        }

    private fun observeTopZappers(eventId: String) {
        topZappersObserveJob?.cancel()
        topZappersObserveJob = viewModelScope.launch {
            noteRepository.observeTopZappers(eventId = eventId).collect {
                setState {
                    copy(
                        topZap = it.firstOrNull()?.asEventZapUiModel() ?: this.topZap,
                        otherZaps = it.drop(n = 1).map { it.asEventZapUiModel() },
                    )
                }
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

    private fun zapArticle(zapAction: UiEvent.ZapArticle) =
        viewModelScope.launch {
        }

    private fun likeArticle() =
        viewModelScope.launch {
        }
}
