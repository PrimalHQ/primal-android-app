package net.primal.data.remote.api.articles

import net.primal.data.remote.api.articles.model.ArticleDetailsRequestBody
import net.primal.data.remote.api.articles.model.ArticleFeedRequestBody
import net.primal.data.remote.api.articles.model.ArticleHighlightsRequestBody
import net.primal.data.remote.api.articles.model.ArticleHighlightsResponse
import net.primal.data.remote.api.articles.model.ArticleResponse

interface ArticlesApi {

    suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse

    suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleResponse

    suspend fun getArticleHighlights(body: ArticleHighlightsRequestBody): ArticleHighlightsResponse
}
