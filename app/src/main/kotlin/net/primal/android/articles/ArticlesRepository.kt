package net.primal.android.articles

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.nostr.model.NostrEventKind

class ArticlesRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val articlesApi: ArticlesApi,
) {

    suspend fun fetchFeedExample(userId: String) =
        withContext(dispatchers.io()) {
            articlesApi.getArticleFeed(
                body = ArticleFeedRequestBody(
                    limit = 20,
                    since = 0,
                    userId = userId,
                    feedUserId = userId,
                ),
            )
        }

    suspend fun fetchBlogContentAndReplies(
        userId: String,
        authorUserId: String,
        identifier: String,
    ) = withContext(dispatchers.io()) {
        articlesApi.getArticleDetails(
            body = ArticleDetailsRequestBody(
                userId = userId,
                authorUserId = authorUserId,
                identifier = identifier,
                kind = NostrEventKind.LongFormContent.value,
                limit = 100,
            ),
        )
    }
}
