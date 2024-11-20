package net.primal.android.articles.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.articles.feed.ui.FeedArticleUi

interface ArticleFeedContract {

    data class UiState(
        val articles: Flow<PagingData<FeedArticleUi>>,
        val paywall: Boolean = false,
    )
}
