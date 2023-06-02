package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.FeedRemoteMediator
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) {

    fun feedByDirectivePaged(feedDirective: String): Flow<PagingData<FeedPost>> {
        return createPager(feedDirective = feedDirective).flow
    }

    fun observeFeeds(): Flow<List<Feed>> = database.feeds().observeAllFeeds()

    suspend fun findFeedByDirective(feedDirective: String) = database.feeds().findFeedByDirective(feedDirective = feedDirective)

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(feedDirective: String) =
        Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = FeedRemoteMediator(
                feedDirective = feedDirective,
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = {
                database.feedPosts().allPostsByFeedDirective(feedDirective = feedDirective)
            },
        )

    companion object {
        private const val DEFAULT_PAGE_SIZE = 5
    }

}
