package net.primal.data.remote.api.feed

import net.primal.data.remote.api.feed.model.FeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.api.feed.model.MultiKindFeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.MultiKindThreadRequestBody
import net.primal.data.remote.api.feed.model.ThreadRequestBody

interface FeedApi {

    suspend fun getFeedBySpec(body: FeedBySpecRequestBody): FeedResponse

    suspend fun getMultiKindFeedBySpec(body: MultiKindFeedBySpecRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): FeedResponse

    suspend fun getMultiKindThread(body: MultiKindThreadRequestBody): FeedResponse

    suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean = true): FeedResponse
}
