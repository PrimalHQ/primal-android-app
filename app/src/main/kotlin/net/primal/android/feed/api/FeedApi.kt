package net.primal.android.feed.api

import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.api.model.ThreadResponse

interface FeedApi {

    suspend fun getFeed(body: FeedRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): ThreadResponse

}