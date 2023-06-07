package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.FeedRemoteMediator
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.isMostZappedFeed
import net.primal.android.feed.isPopularFeed
import net.primal.android.feed.isTrendingFeed
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) {

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

    private fun pagingSourceByDirective(
        database: PrimalDatabase,
        feedDirective: String
    ): PagingSource<Int, FeedPost> {
        return when {
            feedDirective.isTrendingFeed() -> database.feedPosts()
                .allPostsByFeedDirectiveOrderByScore24h(
                    feedDirective = feedDirective
                )

            feedDirective.isMostZappedFeed() -> database.feedPosts()
                .allPostsByFeedDirectiveOrderBySatsZapped(
                    feedDirective = feedDirective
                )

            feedDirective.isPopularFeed() -> database.feedPosts()
                .allPostsByFeedDirectiveOrderByScore(
                    feedDirective = feedDirective
                )

            else -> database.feedPosts()
                .allPostsByFeedDirectiveOrderByCreatedAt(
                    feedDirective = feedDirective
                )
        }
    }

    fun feedByDirective(feedDirective: String): Flow<PagingData<FeedPost>> {
        return createPager(feedDirective = feedDirective) {
            pagingSourceByDirective(database = database, feedDirective = feedDirective)
        }.flow
    }

    fun observeFeeds(): Flow<List<Feed>> = database.feeds().observeAllFeeds()

    suspend fun findFeedByDirective(feedDirective: String) =
        database.feeds().findFeedByDirective(feedDirective = feedDirective)

}
