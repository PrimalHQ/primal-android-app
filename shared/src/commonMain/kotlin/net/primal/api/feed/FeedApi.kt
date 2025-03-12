package net.primal.api.feed

import net.primal.api.feed.model.FeedBySpecRequestBody
import net.primal.api.feed.model.FeedResponse
import net.primal.api.feed.model.ThreadRequestBody

interface FeedApi {

    suspend fun getFeedBySpec(body: FeedBySpecRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): FeedResponse

    suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean = true): FeedResponse
}
