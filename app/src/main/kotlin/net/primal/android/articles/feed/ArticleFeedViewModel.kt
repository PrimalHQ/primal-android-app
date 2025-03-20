package net.primal.android.articles.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ArticleFeedContract.UiState
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.isPremiumFeedSpec

@HiltViewModel(assistedFactory = ArticleFeedViewModel.Factory::class)
class ArticleFeedViewModel @AssistedInject constructor(
    @Assisted private val spec: String,
    private val articleRepository: ArticleRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(spec: String): ArticleFeedViewModel
    }

    private fun buildFeedByDirective() =
        articleRepository.feedBySpec(userId = activeAccountStore.activeUserId(), feedSpec = spec)
            .map { it.map { article -> article.mapAsFeedArticleUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(articles = buildFeedByDirective()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() {
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(paywall = spec.isPremiumFeedSpec() && !it.hasPremiumMembership())
                }
            }
        }
    }
}
