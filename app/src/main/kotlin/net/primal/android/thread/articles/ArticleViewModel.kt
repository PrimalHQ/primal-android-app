package net.primal.android.thread.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.bookmarks.BookmarksRepository
import net.primal.android.bookmarks.domain.BookmarkType
import net.primal.android.core.errors.UiError
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.thread.articles.ArticleContract.UiEvent
import net.primal.android.thread.articles.ArticleContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val bookmarksRepository: BookmarksRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
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
                }
            }
        }

    private fun mute(uiEvent: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                mutedUserRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = uiEvent.userId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
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
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
            } catch (error: NostrPublishException) {
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
            } catch (error: BookmarksRepository.BookmarksListNotFound) {
                Timber.w(error)
                setState { copy(shouldApproveBookmark = true) }
            } catch (error: MissingPrivateKeyException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }
}
