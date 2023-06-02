package net.primal.android.feed.api

import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse

interface FeedApi {

    suspend fun getFeed(body: FeedRequestBody): FeedResponse

}