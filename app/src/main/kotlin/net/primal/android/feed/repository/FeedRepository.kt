package net.primal.android.feed.repository

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.primal.model.request.FeedRequest
import net.primal.android.nostr.primal.PrimalApi
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val cachingServiceApi: PrimalApi,
    private val database: PrimalDatabase,
) {

    fun observeEventsCount() = database.events().observeCount()

    fun fetchLatestEvents() {
        cachingServiceApi.requestFeedUpdates(
            request = FeedRequest(
                userPubKey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812",
                pubKey = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
            )
        )
    }

}
