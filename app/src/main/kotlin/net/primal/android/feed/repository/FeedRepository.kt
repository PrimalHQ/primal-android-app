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
    private val primalApi: PrimalApi,
    private val database: PrimalDatabase,
) {

    fun feedByFeedHexPaged(feedHex: String) = createPager {
        database.feedPosts().allPostsByFeedHex(feedHex = feedHex)
    }.flow

    fun fetchLatestPosts(feedHex: String) {
        primalApi.requestFeedUpdates(
            request = FeedRequest(
                pubKey = feedHex,
                userPubKey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812",
            )
        )
    }

    fun fetchDefaultAppSettings() {
        primalApi.requestDefaultAppSettings()
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
