package net.primal.android.articles.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.api.model.ArticleResponse
import net.primal.android.articles.db.Article
import net.primal.android.articles.db.ArticleFeedCrossRef
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.primal.retryNetworkCall
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.mapNotNullAsArticleDataPO
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.android.notes.db.FeedPostRemoteKey

@OptIn(ExperimentalPagingApi::class)
class ArticleFeedMediator(
    private val userId: String,
    private val feedSpec: String,
    private val articlesApi: ArticlesApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : RemoteMediator<Int, Article>() {

    private val lastRequests: MutableMap<LoadType, Pair<ArticleFeedRequestBody, Long>> = mutableMapOf()

    override suspend fun initialize(): InitializeAction {
        val latestRemoteKey = withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().findLatestByDirective(directive = feedSpec)
        }

        return latestRemoteKey?.let {
            if (it.cachedAt.isTimestampOlderThen(duration = INITIALIZE_CACHE_EXPIRY)) {
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    @Suppress("ReturnCount")
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Article>): MediatorResult {
        val nextUntil = when (loadType) {
            LoadType.APPEND -> findLastRemoteKey()?.sinceId
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.REFRESH -> null
        }

        return try {
            val response = fetchArticles(
                pageSize = state.config.pageSize,
                nextUntil = nextUntil,
                loadType = loadType,
            )

            processAndPersistToDatabase(response = response, clearFeed = loadType == LoadType.REFRESH)

            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: WssException) {
            MediatorResult.Error(error)
        } catch (_: RepeatingRequestBodyException) {
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    @Throws(RepeatingRequestBodyException::class)
    private suspend fun fetchArticles(
        pageSize: Int,
        nextUntil: Long?,
        loadType: LoadType,
    ): ArticleResponse {
        val request = ArticleFeedRequestBody(
            spec = feedSpec,
            userId = userId,
            limit = pageSize,
            until = nextUntil,
        )

        lastRequests[loadType]?.let { (lastRequest, lastRequestAt) ->
            if (request == lastRequest && !lastRequestAt.isRequestCacheExpired() && loadType != LoadType.REFRESH) {
                throw RepeatingRequestBodyException()
            }
        }

        val response = withContext(dispatcherProvider.io()) {
            retryNetworkCall {
                articlesApi.getArticleFeed(
                    body = request,
                )
            }
        }

        lastRequests[loadType] = request to Instant.now().epochSecond
        return response
    }

    private suspend fun findLastRemoteKey(): FeedPostRemoteKey? =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().findLatestByDirective(directive = feedSpec)
        }

    private suspend fun processAndPersistToDatabase(response: ArticleResponse, clearFeed: Boolean) {
        val connections = response.articles.mapNotNullAsArticleDataPO().map {
            ArticleFeedCrossRef(
                spec = feedSpec,
                articleId = it.articleId,
                articleAuthorId = it.authorId,
            )
        }

        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                if (clearFeed) {
                    database.feedPostsRemoteKeys().deleteByDirective(feedSpec)
                    database.articleFeedsConnections().deleteConnectionsBySpec(spec = feedSpec)
                }

                database.articleFeedsConnections().connect(data = connections)

                response.persistToDatabaseAsTransaction(
                    userId = userId,
                    database = database,
                )
            }

            response.articles.processRemoteKeys(pagingEvent = response.paging)
        }
    }

    private fun List<NostrEvent>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
        if (pagingEvent?.sinceId != null && pagingEvent.untilId != null) {
            val remoteKeys = this.map {
                FeedPostRemoteKey(
                    eventId = it.id,
                    directive = feedSpec,
                    sinceId = pagingEvent.sinceId,
                    untilId = pagingEvent.untilId,
                    cachedAt = Instant.now().epochSecond,
                )
            }
            database.feedPostsRemoteKeys().upsert(remoteKeys)
        }
    }

    private fun Long.isTimestampOlderThen(duration: Long) = (Instant.now().epochSecond - this) > duration

    private fun Long.isRequestCacheExpired() = isTimestampOlderThen(duration = LAST_REQUEST_EXPIRY)

    private inner class RepeatingRequestBodyException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val INITIALIZE_CACHE_EXPIRY = 3.minutes.inWholeSeconds
    }
}
