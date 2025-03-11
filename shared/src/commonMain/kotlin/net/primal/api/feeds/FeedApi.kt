package net.primal.api.feeds

import net.primal.api.feeds.model.FeedBySpecRequestBody
import net.primal.api.feeds.model.FeedResponse
import net.primal.api.feeds.model.ThreadRequestBody

interface FeedApi {

    suspend fun getFeedBySpec(body: FeedBySpecRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): FeedResponse

    suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean = true): FeedResponse
}
