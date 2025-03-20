package net.primal.data.remote.api.articles

interface ArticlesApi {

    suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse

    suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleResponse

    suspend fun getArticleHighlights(body: ArticleHighlightsRequestBody): ArticleHighlightsResponse
}
