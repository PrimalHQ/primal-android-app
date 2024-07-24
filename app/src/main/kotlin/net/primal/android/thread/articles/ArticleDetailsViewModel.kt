package net.primal.android.thread.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticlesRepository
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.feed.repository.FeedRepository
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
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.thread.articles.ArticleDetailsContract.ArticleDetailsError
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ArticleDetailsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val readsRepository: ArticlesRepository,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val naddr = Nip19TLV.parseAsNaddr(savedStateHandle.naddrOrThrow)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()

        if (naddr == null) {
            setState { copy(error = ArticleDetailsError.InvalidNaddr) }
        } else {
            observeArticle(naddr)
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> fetchData(naddr)
                    UiEvent.DismissErrors -> setState { copy(error = null) }
                }
            }
        }

    private fun fetchData(naddr: Naddr?) =
        viewModelScope.launch {
            if (naddr != null) {
                try {
                    readsRepository.fetchBlogContentAndReplies(
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
            readsRepository.observeArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
                .collect { article ->
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

                    setState { copy(markdownContent = article.data.content) }
                }
        }
}
