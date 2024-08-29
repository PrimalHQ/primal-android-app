package net.primal.android.articles

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.mediator.ArticleFeedMediator
import net.primal.android.articles.api.mediator.persistArticleCommentsToDatabase
import net.primal.android.articles.api.mediator.persistToDatabaseAsTransaction
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.db.Article
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.user.accounts.active.ActiveAccountStore

class ArticleRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
) {

    companion object {
        private const val PAGE_SIZE = 25
    }

    fun feedBySpec(feedSpec: String): Flow<PagingData<Article>> {
        return createPager(feedSpec = feedSpec) {
            database.articles().feed(
                spec = feedSpec,
                userId = activeAccountStore.activeUserId(),
            )
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(feedSpec: String, pagingSourceFactory: () -> PagingSource<Int, Article>) =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE * 5,
                enablePlaceholders = true,
            ),
            remoteMediator = ArticleFeedMediator(
                userId = activeAccountStore.activeUserId(),
                feedSpec = feedSpec,
                articlesApi = articlesApi,
                database = database,
                dispatcherProvider = dispatchers,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    suspend fun fetchArticleAndComments(articleId: String, articleAuthorId: String) =
        withContext(dispatchers.io()) {
            val userId = activeAccountStore.activeUserId()
            val response = articlesApi.getArticleDetails(
                body = ArticleDetailsRequestBody(
                    userId = userId,
                    authorUserId = articleAuthorId,
                    identifier = articleId,
                    kind = NostrEventKind.LongFormContent.value,
                    limit = 100,
                ),
            )

            response.persistToDatabaseAsTransaction(userId = userId, database = database)
            response.persistArticleCommentsToDatabase(
                articleId = articleId,
                articleAuthorId = articleAuthorId,
                database = database,
            )
        }

    suspend fun observeArticle(articleId: String, articleAuthorId: String) =
        withContext(dispatchers.io()) {
            database.articles().observeArticle(articleId = articleId, authorId = articleAuthorId)
                .distinctUntilChanged()
                .filterNotNull()
        }

    suspend fun observeArticleComments(articleId: String, articleAuthorId: String) =
        withContext(dispatchers.io()) {
            val userId = activeAccountStore.activeUserId()
            database.threadConversations().observeArticleComments(
                articleId = articleId,
                articleAuthorId = articleAuthorId,
                userId = userId,
            )
        }

    suspend fun observeArticleByCommentId(commentNoteId: String): Flow<Article?> =
        withContext(dispatchers.io()) {
            val crossRef = database.threadConversations().findCrossRefByCommentId(commentNoteId = commentNoteId)
            if (crossRef != null) {
                database.articles()
                    .observeArticle(
                        articleId = crossRef.articleId,
                        authorId = crossRef.articleAuthorId,
                    )
                    .distinctUntilChanged()
            } else {
                flowOf(null)
            }
        }
}
