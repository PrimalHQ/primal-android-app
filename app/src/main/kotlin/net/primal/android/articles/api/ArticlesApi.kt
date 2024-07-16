package net.primal.android.articles.api

import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleDetailsResponse
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.api.model.ArticleFeedResponse

interface ArticlesApi {

    suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleDetailsResponse

    suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleFeedResponse
}
