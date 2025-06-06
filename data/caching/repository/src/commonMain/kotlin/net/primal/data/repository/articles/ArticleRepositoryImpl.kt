package net.primal.data.repository.articles

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.reads.Article as ArticlePO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.api.articles.model.ArticleDetailsRequestBody
import net.primal.data.remote.api.articles.model.ArticleHighlightsRequestBody
import net.primal.data.repository.articles.paging.ArticleFeedMediator
import net.primal.data.repository.articles.processors.persistArticleCommentsToDatabase
import net.primal.data.repository.articles.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.local.asArticleDO
import net.primal.data.repository.mappers.local.mapAsFeedPostDO
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.posts.FeedPost
import net.primal.domain.reads.Article as ArticleDO
import net.primal.domain.reads.ArticleRepository

class ArticleRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
) : ArticleRepository {

    override fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<ArticleDO>> {
        return createPager(userId = userId, feedSpec = feedSpec) {
            database.articles().feed(
                spec = feedSpec,
                userId = userId,
            )
        }.flow.map { it.map { it.asArticleDO() } }
    }

    override suspend fun fetchArticleAndComments(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    ) = withContext(dispatcherProvider.io()) {
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

    override suspend fun fetchArticleHighlights(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    ) = withContext(dispatcherProvider.io()) {
        val highlightsResponse = articlesApi.getArticleHighlights(
            body = ArticleHighlightsRequestBody(
                userId = userId,
                identifier = articleId,
                authorUserId = articleAuthorId,
                kind = NostrEventKind.LongFormContent.value,
            ),
        )

        highlightsResponse.persistToDatabaseAsTransaction(database = database)
    }

    override suspend fun observeArticle(articleId: String, articleAuthorId: String): Flow<ArticleDO> =
        withContext(dispatcherProvider.io()) {
            database.articles().observeArticle(articleId = articleId, authorId = articleAuthorId)
                .distinctUntilChanged()
                .filterNotNull()
                .map { it.asArticleDO() }
        }

    override suspend fun getArticleByATag(aTag: String): ArticleDO? =
        withContext(dispatcherProvider.io()) {
            database.articles().findArticleByATag(articleATag = aTag)?.asArticleDO()
        }

    override suspend fun observeArticleByEventId(eventId: String, articleAuthorId: String): Flow<ArticleDO> =
        withContext(dispatcherProvider.io()) {
            database.articles().observeArticleByEventId(eventId = eventId, authorId = articleAuthorId)
                .distinctUntilChanged()
                .filterNotNull()
                .map { it.asArticleDO() }
        }

    override suspend fun observeArticleComments(
        userId: String,
        articleId: String,
        articleAuthorId: String,
    ): Flow<List<FeedPost>> =
        withContext(dispatcherProvider.io()) {
            database.threadConversations().observeArticleComments(
                articleId = articleId,
                articleAuthorId = articleAuthorId,
                userId = userId,
            ).map { it.map { it.mapAsFeedPostDO() } }
        }

    override suspend fun deleteArticleByATag(articleATag: String) =
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                val article = database.articles().findAndDeleteArticleByATag(articleATag = articleATag)
                article?.data?.let {
                    database.feedPostsRemoteKeys().deleteAllByEventId(it.eventId)
                    database.threadConversations()
                        .deleteArticleCrossRefs(articleId = it.articleId, articleAuthorId = it.authorId)

                    database.eventStats().deleteByEventId(eventId = it.eventId)
                    database.eventUserStats().deleteByEventId(eventId = it.eventId)
                }

                database.articleFeedsConnections().deleteConnectionsByATag(articleATag = articleATag)
            }
        }

    override suspend fun observeArticleByCommentId(commentNoteId: String): Flow<ArticleDO?> =
        withContext(dispatcherProvider.io()) {
            val crossRef = database.threadConversations().findCrossRefByCommentId(commentNoteId = commentNoteId)
            if (crossRef != null) {
                database.articles()
                    .observeArticle(
                        articleId = crossRef.articleId,
                        authorId = crossRef.articleAuthorId,
                    )
                    .distinctUntilChanged()
                    .map { it?.asArticleDO() }
            } else {
                flowOf(null)
            }
        }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        feedSpec: String,
        pagingSourceFactory: () -> PagingSource<Int, ArticlePO>,
    ) = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE * 2,
            initialLoadSize = PAGE_SIZE * 5,
            enablePlaceholders = true,
        ),
        remoteMediator = ArticleFeedMediator(
            userId = userId,
            feedSpec = feedSpec,
            articlesApi = articlesApi,
            database = database,
            dispatcherProvider = dispatcherProvider,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )

    companion object {
        private const val PAGE_SIZE = 25
    }
}
