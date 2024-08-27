package net.primal.android.articles.reads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.db.ArticleFeed
import net.primal.android.articles.reads.ReadsScreenContract.UiEvent
import net.primal.android.articles.reads.ReadsScreenContract.UiState
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager

@HiltViewModel
class ReadsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeBadgesUpdates()
        observeEvents()
        loadDefaultReadsFeed()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ChangeFeed -> {
                        setState { copy(activeFeed = it.feed) }
                    }
                }
            }
        }
    }

    private fun loadDefaultReadsFeed() =
        viewModelScope.launch {
            val defaultFeed = articleRepository.firstFeed()?.asFeedUi()
            setState { copy(activeFeed = defaultFeed) }
        }

    private fun ArticleFeed.asFeedUi() =
        FeedUi(
            directive = this.spec,
            name = this.name,
            description = this.description,
        )

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(activeAccountAvatarCdnImage = it.avatarCdnImage)
                }
            }
        }

    private fun observeBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }
}
