package net.primal.android.articles.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ArticleFeedContract.UiState
import net.primal.android.articles.feed.di.ArticleFeedViewModelFactory
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi

@HiltViewModel(assistedFactory = ArticleFeedViewModelFactory::class)
class ArticleFeedViewModel @AssistedInject constructor(
    @Assisted private val spec: String,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private fun buildFeedByDirective() =
        articleRepository.feedBySpec(feedSpec = spec)
            .map { it.map { article -> article.mapAsFeedArticleUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(articles = buildFeedByDirective()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }
}
