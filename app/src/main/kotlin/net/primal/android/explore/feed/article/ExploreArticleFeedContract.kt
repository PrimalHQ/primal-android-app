package net.primal.android.explore.feed.article

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.articles.feed.ui.FeedArticleUi

interface ExploreArticleFeedContract {

    data class UiState(
        val feedSpec: String,
    )
}
