package net.primal.android.feed.api

import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.NoteActionsRequestBody
import net.primal.android.feed.api.model.NoteActionsResponse
import net.primal.android.feed.api.model.NoteZapsRequestBody
import net.primal.android.feed.api.model.NoteZapsResponse
import net.primal.android.feed.api.model.ThreadRequestBody

interface FeedApi {

    suspend fun getFeed(body: FeedRequestBody): FeedResponse

    suspend fun getThread(body: ThreadRequestBody): FeedResponse

    suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean = true): FeedResponse

    suspend fun getNoteZaps(body: NoteZapsRequestBody): NoteZapsResponse

    suspend fun getNoteActions(body: NoteActionsRequestBody): NoteActionsResponse
}
