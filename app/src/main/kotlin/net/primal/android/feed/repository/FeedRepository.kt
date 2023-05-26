package net.primal.android.feed.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
 import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import net.primal.android.nostr.primal.PrimalApi
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val primalApi: PrimalApi,
    private val database: PrimalDatabase,
) {

    fun feedByFeedIdPaged(feedId: String): Flow<PagingData<FeedPost>> {
        fetchLatestPosts(feedId = feedId)
        return createPager {
            database.feedPosts().allPostsByFeedId(feedId = feedId)
        }.flow
    }

    private fun fetchLatestPosts(feedId: String) {
        primalApi.requestFeedUpdates(
            feedHex = feedId,
            // User hard-coded to Nostr Highlights
            userHex = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a",
        )
    }

    fun observeFeeds(): Flow<List<Feed>> = database.feeds().observeAllFeeds()

    suspend fun findFeedById(feedId: String) = database.feeds().findFeedById(feedId = feedId)

    private fun createPager(pagingSourceFactory: () -> PagingSource<Int, FeedPost>) =
        Pager(
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = pagingSourceFactory,
        )

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

}
