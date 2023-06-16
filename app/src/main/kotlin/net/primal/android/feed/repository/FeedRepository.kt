package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.mediator.FeedRemoteMediator
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.db.sql.LatestFeedQueryBuilder
import net.primal.android.feed.isLatestFeed
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) {

    fun observeFeeds(): Flow<List<Feed>> = database.feeds().observeAllFeeds()

    suspend fun findFeedByDirective(feedDirective: String) =
        database.feeds().findFeedByDirective(feedDirective = feedDirective)

    fun feedByDirective(feedDirective: String): Flow<PagingData<FeedPost>> {
        return createPager(feedDirective = feedDirective) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(feedDirective = feedDirective).feedQuery()
            )
        }.flow
    }

    fun findNewestPosts(feedDirective: String, limit: Int) =
        database.feedPosts().newestFeedPosts(
            query = feedQueryBuilder(feedDirective = feedDirective).newestFeedPostsQuery(limit = limit)
        )

    fun observeNewFeedPostsSyncUpdates(feedDirective: String, since: Long) =
        database.feedPostsSync()
            .observeFeedDirective(feedDirective = feedDirective, since = since)
            .filterNotNull()

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        feedDirective: String,
        pagingSourceFactory: () -> PagingSource<Int, FeedPost>,
    ) =
        Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false
            ),
            remoteMediator = FeedRemoteMediator(
                feedDirective = feedDirective,
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun feedQueryBuilder(feedDirective: String): FeedQueryBuilder = when {
        feedDirective.isLatestFeed() -> LatestFeedQueryBuilder(feedDirective = feedDirective)
        else -> ExploreFeedQueryBuilder(feedDirective = feedDirective)
    }

}
