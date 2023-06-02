package net.primal.android.feed.api

import kotlinx.serialization.encodeToString
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.serialization.NostrJson
import javax.inject.Inject

class FeedApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : FeedApi {

    override suspend fun getFeed(body: FeedRequestBody): FeedResponse {
        val queryResult = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "feed_directive",
                options = NostrJson.encodeToString(body)
            )
        )

        return FeedResponse(
            paging = queryResult.pagingEvent,
            nostrEvents = queryResult.nostrEvents,
            primalEvents = queryResult.primalEvents,
        )
    }

}
