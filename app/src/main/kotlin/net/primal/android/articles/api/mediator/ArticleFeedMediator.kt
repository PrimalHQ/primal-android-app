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
import net.primal.android.articles.db.Article
import net.primal.android.articles.db.ArticleFeedCrossRef
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.mapNotNullAsArticleDataPO
import net.primal.android.nostr.ext.orderByPagingIfNotNull
import net.primal.android.notes.db.FeedPostRemoteKey
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.data.remote.api.articles.ArticleFeedRequestBody
import net.primal.data.remote.api.articles.ArticleResponse
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.model.ContentPrimalPaging
import timber.log.Timber

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
            database.feedPostsRemoteKeys().findLatestByDirective(ownerId = userId, directive = feedSpec)
        }

        return latestRemoteKey?.let {
            if (it.cachedAt.isTimestampOlderThan(duration = INITIALIZE_CACHE_EXPIRY)) {
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    @Suppress("ReturnCount")
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Article>): MediatorResult {
        val nextUntil = when (loadType) {
            LoadType.APPEND -> findLastRemoteKey(state = state)?.sinceId
                ?: run {
                    Timber.d("APPEND no remote key found exit.")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

            LoadType.PREPEND -> {
                Timber.d("PREPEND end of pagination exit.")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

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
            Timber.d("RepeatingRequestBody exit.")
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

    private suspend fun findLastRemoteKey(state: PagingState<Int, Article>): FeedPostRemoteKey? {
        val lastItemATag = state.lastItemOrNull()?.data?.aTag
            ?: findLastItemOrNull()?.articleATag

        return withContext(dispatcherProvider.io()) {
            lastItemATag?.let { database.feedPostsRemoteKeys().findByEventId(ownerId = userId, eventId = lastItemATag) }
                ?: database.feedPostsRemoteKeys().findLatestByDirective(ownerId = userId, directive = feedSpec)
        }
    }

    private suspend fun findLastItemOrNull(): ArticleFeedCrossRef? =
        withContext(dispatcherProvider.io()) {
            database.articleFeedsConnections().findLastBySpec(ownerId = userId, spec = feedSpec)
        }

    private suspend fun processAndPersistToDatabase(response: ArticleResponse, clearFeed: Boolean) {
        val connections = response.articles
            .orderByPagingIfNotNull(pagingEvent = response.paging)
            .mapNotNullAsArticleDataPO().map {
                ArticleFeedCrossRef(
                    ownerId = userId,
                    spec = feedSpec,
                    articleATag = it.aTag,
                    articleAuthorId = it.authorId,
                )
            }

        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                if (clearFeed) {
                    database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
                    database.articleFeedsConnections().deleteConnectionsBySpec(ownerId = userId, spec = feedSpec)
                }

                database.articleFeedsConnections().connect(data = connections)

                response.persistToDatabaseAsTransaction(
                    userId = userId,
                    database = database,
                )
            }

            connections.processRemoteKeys(pagingEvent = response.paging)
        }
    }

    private fun List<ArticleFeedCrossRef>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
        val sinceId = pagingEvent?.sinceId
        val untilId = pagingEvent?.untilId
        if (sinceId != null && untilId != null) {
            val remoteKeys = this.map {
                FeedPostRemoteKey(
                    ownerId = userId,
                    eventId = it.articleATag,
                    directive = feedSpec,
                    sinceId = sinceId,
                    untilId = untilId,
                    cachedAt = Instant.now().epochSecond,
                )
            }
            database.feedPostsRemoteKeys().upsert(remoteKeys)
        }
    }

    private fun Long.isTimestampOlderThan(duration: Long) = (Instant.now().epochSecond - this) > duration

    private fun Long.isRequestCacheExpired() = isTimestampOlderThan(duration = LAST_REQUEST_EXPIRY)

    private inner class RepeatingRequestBodyException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val INITIALIZE_CACHE_EXPIRY = 3.minutes.inWholeSeconds
    }
}
