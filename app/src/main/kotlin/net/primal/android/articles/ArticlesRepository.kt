package net.primal.android.articles

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.mediator.ArticleFeedMediator
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.db.Article
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.user.accounts.active.ActiveAccountStore

class ArticlesRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
) {

    companion object {
        private const val PAGE_SIZE = 25
    }

    fun defaultFeed(): Flow<PagingData<Article>> {
        return createPager {
            database.articles().feed(
                query = SimpleSQLiteQuery(
                    query = """
                        SELECT * 
                        FROM ArticleData
                        ORDER BY ArticleData.publishedAt DESC
                    """.trimIndent(),
                ),
            )
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(pagingSourceFactory: () -> PagingSource<Int, Article>) =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE * 5,
                enablePlaceholders = true,
            ),
            remoteMediator = ArticleFeedMediator(
                dispatcherProvider = dispatchers,
                userId = activeAccountStore.activeUserId(),
                articlesApi = articlesApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

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
