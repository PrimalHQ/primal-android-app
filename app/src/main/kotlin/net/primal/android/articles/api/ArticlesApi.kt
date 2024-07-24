package net.primal.android.articles.api

import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.api.model.ArticleResponse

interface ArticlesApi {

    suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse

    suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleResponse
}
