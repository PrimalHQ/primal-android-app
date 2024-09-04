package net.primal.android.notes.api

import net.primal.android.notes.api.model.FeedRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.api.model.ThreadRequestBody

interface FeedApi {

    @Deprecated("Use getFeedMega")
    suspend fun getFeed(body: FeedRequestBody): FeedResponse

    suspend fun getFeedMega(body: FeedRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): FeedResponse

    suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean = true): FeedResponse
}
