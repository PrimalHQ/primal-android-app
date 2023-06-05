package net.primal.android.feed.api

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
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

        val nostrEvents = queryResult.nostrEvents
        val nostrEventsMap = nostrEvents.groupBy { NostrEventKind.valueOf(it.kind) }

        val primalEvents = queryResult.primalEvents
        val primalEventsMap = primalEvents.groupBy { NostrEventKind.valueOf(it.kind) }
        val pagingEvents = primalEventsMap[NostrEventKind.PrimalPaging] ?: emptyList()

        return FeedResponse(
            paging = pagingEvents.firstPagingContentOrNull(),
            allNostrEvents = nostrEvents,
            metadata = nostrEventsMap[NostrEventKind.Metadata] ?: emptyList(),
            shortTextNotes = nostrEventsMap[NostrEventKind.ShortTextNote] ?: emptyList(),
            reposts = nostrEventsMap[NostrEventKind.Reposts] ?: emptyList(),
            allPrimalEvents = primalEvents,
            eventStats = primalEventsMap[NostrEventKind.PrimalEventStats] ?: emptyList(),
            eventUserStats = primalEventsMap[NostrEventKind.PrimalEventUserStats] ?: emptyList(),
            eventResources = primalEventsMap[NostrEventKind.PrimalEventResources] ?: emptyList(),
            referencedEvents = primalEventsMap[NostrEventKind.PrimalReferencedEvent] ?: emptyList(),
        )

    }

    private fun List<PrimalEvent>?.firstPagingContentOrNull(): ContentPrimalPaging? {
        val pagingContent = this?.firstOrNull()?.content ?: return null
        return try {
            NostrJson.decodeFromString(pagingContent)
        } catch (error: IllegalArgumentException) {
            null
        }
    }

}
