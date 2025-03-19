package net.primal.android.notes.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.feeds.domain.isNotesBookmarkFeedSpec
import net.primal.android.feeds.domain.isProfileAuthoredNoteRepliesFeedSpec
import net.primal.android.feeds.domain.isProfileAuthoredNotesFeedSpec
import net.primal.android.feeds.domain.supportsNoteReposts
import net.primal.android.feeds.domain.supportsUpwardsNotesPagination
import net.primal.android.notes.api.FeedApi
import net.primal.android.notes.api.model.FeedBySpecRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.db.FeedPost
import net.primal.android.notes.db.FeedPostRemoteKey
import net.primal.android.notes.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.notes.db.sql.ExploreFeedQueryBuilder
import net.primal.android.notes.db.sql.FeedQueryBuilder
import net.primal.android.notes.repository.FeedProcessor
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.utils.retryNetworkCall
import timber.log.Timber

@ExperimentalPagingApi
class NoteFeedRemoteMediator(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedSpec: String,
    private val userId: String,
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, FeedPost>() {

    private val feedQueryBuilder: FeedQueryBuilder = when {
        feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
            feedSpec = feedSpec,
            userPubkey = userId,
        )

        else -> ExploreFeedQueryBuilder(
            feedSpec = feedSpec,
            userPubkey = userId,
        )
    }

    private val lastRequests: MutableMap<LoadType, Pair<FeedBySpecRequestBody, Long>> = mutableMapOf()

    private val feedProcessor: FeedProcessor = FeedProcessor(feedSpec = feedSpec, database = database)

    private suspend fun String.isLastCacheTimestampOlderThan(duration: Duration): Boolean {
        val lastCachedAt = withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys()
                .lastCachedAt(ownerId = userId, directive = this@isLastCacheTimestampOlderThan)
        } ?: return true

        return lastCachedAt < Instant.now().minusSeconds(duration.inWholeSeconds).epochSecond
    }

    private suspend fun shouldResetLocalCache() =
        when {
            feedSpec.isNotesBookmarkFeedSpec() -> true
            feedSpec.isProfileAuthoredNotesFeedSpec() -> true
            feedSpec.isProfileAuthoredNoteRepliesFeedSpec() -> true
            feedSpec.supportsUpwardsNotesPagination() -> feedSpec.isLastCacheTimestampOlderThan(duration = 24.hours)
            else -> feedSpec.isLastCacheTimestampOlderThan(duration = 3.minutes)
        }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache() -> {
                clearFeedSpec(feedSpec = feedSpec)
                InitializeAction.LAUNCH_INITIAL_REFRESH
            }

            else -> {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun load(loadType: LoadType, state: PagingState<Int, FeedPost>): MediatorResult {
        Timber.i("feed_spec $feedSpec load called ($loadType)")
        if (loadType == LoadType.PREPEND) {
            Timber.w("feed_spec $feedSpec load exit 9")
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val remoteKey = when (loadType) {
//                LoadType.PREPEND -> findFirstFeedPostRemoteKey(state = state)
                LoadType.APPEND -> findLastFeedPostRemoteKey(state = state)
                else -> null
            }

            if (remoteKey == null && loadType != LoadType.REFRESH) {
                throw RemoteKeyNotFoundException()
            }

            withContext(dispatcherProvider.io()) {
                syncFeed(
                    loadType = loadType,
                    pagingState = state,
                    remoteKey = remoteKey,
                )
            }

            Timber.w("feed_spec $feedSpec load exit 6")
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: IOException) {
            Timber.w(error, "feed_spec $feedSpec load exit 7")
            MediatorResult.Error(error)
        } catch (error: NostrNoticeException) {
            Timber.w(error, "feed_spec $feedSpec load exit 8")
            MediatorResult.Error(error)
        } catch (error: NoSuchFeedPostException) {
            Timber.w(error, "feed_spec $feedSpec load exit 2")
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (error: RemoteKeyNotFoundException) {
            Timber.w(error, "feed_spec $feedSpec load exit 3")
            MediatorResult.Error(error)
        } catch (error: WssException) {
            Timber.w(error, "feed_spec $feedSpec load exit 5")
            MediatorResult.Error(error)
        } catch (error: RepeatingRequestBodyException) {
            Timber.w(error, "feed_spec $feedSpec load exit 4")
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun clearFeedSpec(feedSpec: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
            database.feedsConnections().deleteConnectionsByDirective(ownerId = userId, feedSpec = feedSpec)
        }

    private suspend fun NoteFeedRemoteMediator.syncFeed(
        loadType: LoadType,
        pagingState: PagingState<Int, FeedPost>,
        remoteKey: FeedPostRemoteKey?,
    ) {
        val pageSize = pagingState.config.pageSize
        val (request, response) = when (loadType) {
            LoadType.REFRESH -> syncRefresh(pageSize = pageSize)
            LoadType.PREPEND -> syncPrepend(remoteKey = remoteKey, pageSize = pageSize)
            LoadType.APPEND -> syncAppend(remoteKey = remoteKey, pageSize = pageSize)
        }

        feedProcessor.processAndPersistToDatabase(
            userId = userId,
            response = response,
            clearFeed = loadType == LoadType.REFRESH,
        )

        lastRequests[loadType] = request to Instant.now().epochSecond
    }

    private suspend fun syncRefresh(pageSize: Int): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
        )
        val response = retryNetworkCall(
            onBeforeDelay = { error -> Timber.w(error, "Attempting FeedRemoteMediator.retry().") },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            response.paging ?: throw WssException("PagingEvent not found.")
            response
        }
        return requestBody to response
    }

    private suspend fun syncPrepend(
        remoteKey: FeedPostRemoteKey?,
        pageSize: Int,
    ): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
            since = remoteKey?.untilId,
            order = "asc",
        )

        lastRequests[LoadType.PREPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retryNetworkCall(
            onBeforeDelay = { error -> Timber.w(error, "Attempting FeedRemoteMediator.retry().") },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private suspend fun syncAppend(
        remoteKey: FeedPostRemoteKey?,
        pageSize: Int,
    ): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
            until = remoteKey?.sinceId,
        )

        lastRequests[LoadType.APPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retryNetworkCall(
            onBeforeDelay = { error -> Timber.w(error, "Attempting FeedRemoteMediator.retry().") },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private fun Long.isRequestCacheExpired() = (Instant.now().epochSecond - this) < LAST_REQUEST_EXPIRY

//    private suspend fun findFirstFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
//        val firstItem = state.firstItemOrNull()
//            ?: newestFeedPostInDatabaseOrNull()
//            ?: throw NoSuchFeedPostException()
//
//        return withContext(dispatcherProvider.io()) {
//            Timber.i(
//                "feed_spec $feedDirective looking for firstItem postId=${firstItem.data.postId}" +
//                    " and repostId=${firstItem.data.repostId}",
//            )
//            database.feedPostsRemoteKeys().find(
//                postId = firstItem.data.postId,
//                repostId = firstItem.data.repostId,
//                directive = feedDirective,
//            )
//        }
//    }

    private suspend fun findLastFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
        val lastItem = state.lastItemOrNull()
            ?: oldestFeedPostInDatabaseOrNull()
            ?: throw NoSuchFeedPostException()

        return withContext(dispatcherProvider.io()) {
            Timber.i(
                "feed_spec $feedSpec looking for lastItem postId=${lastItem.data.postId}" +
                    " and repostId=${lastItem.data.repostId}",
            )
            database.feedPostsRemoteKeys().find(
                ownerId = userId,
                postId = lastItem.data.postId,
                repostId = lastItem.data.repostId,
                directive = feedSpec,
            )
        }
    }

    private suspend fun oldestFeedPostInDatabaseOrNull() =
        withContext(dispatcherProvider.io()) {
            database.feedPosts()
                .oldestFeedPosts(query = feedQueryBuilder.oldestFeedPostsQuery(limit = 1))
                .firstOrNull()
        }

    private inner class NoSuchFeedPostException : RuntimeException()

    private inner class RepeatingRequestBodyException : RuntimeException()

    private inner class RemoteKeyNotFoundException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
    }
}
