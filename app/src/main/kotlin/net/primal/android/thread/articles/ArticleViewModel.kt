package net.primal.android.thread.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.thread.articles.ArticleContract.SideEffect
import net.primal.android.thread.articles.ArticleContract.UiEvent
import net.primal.android.thread.articles.ArticleContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.reads.ArticleRepository
import timber.log.Timber

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val articleRepository: ArticleRepository,
    private val profileRepository: ProfileRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
    private val eventInteractionRepository: EventInteractionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(activeAccountUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState { copy(activeAccountUserId = activeAccountStore.activeUserId()) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.ReportAbuse -> report(it)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    UiEvent.DismissBookmarkConfirmation -> dismissBookmarkConfirmation()
                    is UiEvent.RequestDeleteAction -> requestDelete(it.eventId, it.articleATag, it.authorId)
                }
            }
        }

    private fun mute(uiEvent: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                mutedItemRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = uiEvent.userId,
                )
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingPrivateKey) }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setState { copy(error = UiError.NostrSignUnauthorized) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            }
        }

    private fun report(uiEvent: UiEvent.ReportAbuse) =
        viewModelScope.launch {
            try {
                profileRepository.reportAbuse(
                    userId = activeAccountStore.activeUserId(),
                    reportType = uiEvent.reportType,
                    profileId = uiEvent.authorId,
                    eventId = uiEvent.eventId,
                    articleId = uiEvent.articleId,
                )
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun requestDelete(
        eventId: String,
        articleATag: String,
        authorId: String,
    ) = viewModelScope.launch {
        if (authorId != activeAccountStore.activeUserId()) return@launch
        try {
            val relayHint = relayHintsRepository
                .findRelaysByIds(listOf(eventId)).flatMap { it.relays }.firstOrNull()

            eventInteractionRepository.deleteEvent(
                userId = authorId,
                eventIdentifier = articleATag,
                eventKind = NostrEventKind.LongFormContent,
                relayHint = relayHint,
            )

            articleRepository.deleteArticleByATag(articleATag = articleATag)
            setEffect(SideEffect.ArticleDeleted)
        } catch (error: NostrPublishException) {
            Timber.w(error)
            setState { copy(error = UiError.FailedToPublishDeleteEvent(error)) }
        } catch (error: MissingRelaysException) {
            Timber.w(error)
            setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
        } catch (error: SigningKeyNotFoundException) {
            setState { copy(error = UiError.MissingPrivateKey) }
            Timber.w(error)
        } catch (error: SigningRejectedException) {
            setState { copy(error = UiError.NostrSignUnauthorized) }
            Timber.w(error)
        }
    }

    private fun handleBookmark(event: UiEvent.BookmarkAction) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            try {
                setState { copy(shouldApproveBookmark = false) }
                val isBookmarked = bookmarksRepository.isBookmarked(tagValue = event.articleATag)
                when (isBookmarked) {
                    true -> bookmarksRepository.removeFromBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Article,
                        tagValue = event.articleATag,
                    )

                    false -> bookmarksRepository.addToBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Article,
                        tagValue = event.articleATag,
                    )
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            } catch (error: PublicBookmarksNotFoundException) {
                Timber.w(error)
                setState { copy(shouldApproveBookmark = true) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }
}
