package net.primal.android.articles.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ArticleFeedScreenContract.UiState
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.core.compose.feed.list.FeedUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.wallet.zaps.hasWallet

@HiltViewModel
class ArticleFeedViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val tempFeedSpec = "{\"id\":\"feed-reads\",\"scope\":\"follows\"}"

    private fun buildFeedByDirective() =
        articleRepository.feedBySpec(feedSpec = tempFeedSpec)
            .map { it.map { article -> article.mapAsFeedArticleUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(articles = buildFeedByDirective()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        subscribeToFeeds()
    }

    private fun subscribeToFeeds() =
        viewModelScope.launch {
            articleRepository.observeFeeds().collect { feeds ->
                setState {
                    copy(
                        feeds = feeds.map { FeedUi(directive = it.spec, name = it.name) },
                        feedTitle = feeds.find { it.spec == tempFeedSpec }?.name ?: "",
                    )
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        zappingState = this.zappingState.copy(
                            walletConnected = it.hasWallet(),
                            walletPreference = it.walletPreference,
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            walletBalanceInBtc = it.primalWalletState.balanceInBtc,
                        ),
                    )
                }
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }
}
