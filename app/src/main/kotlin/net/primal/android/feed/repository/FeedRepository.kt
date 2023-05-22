package net.primal.android.feed.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.FeedPost
import net.primal.android.nostr.primal.PrimalApi
import net.primal.android.nostr.primal.model.request.FeedRequest
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val cachingServiceApi: PrimalApi,
    private val database: PrimalDatabase,
) {

    fun observeEventsCount() = database.feedPosts().observeCount()

    fun feedPaged() = createPager { database.feedPosts().allPostsPaged() }.flow

    fun fetchLatestPosts() {
        cachingServiceApi.requestFeedUpdates(
            request = FeedRequest(
                pubKey = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a",
                userPubKey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812",
            )
        )
    }

    private fun createPager(pagingSourceFactory: () -> PagingSource<Int, FeedPost>) =
        Pager(
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = pagingSourceFactory,
        )

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

}
