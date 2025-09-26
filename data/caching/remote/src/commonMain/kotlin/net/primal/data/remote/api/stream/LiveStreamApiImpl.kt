package net.primal.data.remote.api.stream

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.stream.model.FindLiveStreamRequestBody
import net.primal.data.remote.api.stream.model.FindLiveStreamResponse
import net.primal.data.remote.api.stream.model.LiveEventsFromFollowsRequest
import net.primal.data.remote.api.stream.model.LiveFeedRequestBody
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class LiveStreamApiImpl(
    private val primalApiClient: PrimalApiClient,
) : LiveStreamApi {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun subscribeToLiveEvent(
        streamingNaddr: Naddr,
        userId: String,
        contentModerationMode: String,
    ): Flow<LiveFeedResponse> {
        val subscriptionId = Uuid.random().toPrimalSubscriptionId()
        return primalApiClient
            .subscribeBufferedOnInactivity(
                subscriptionId = subscriptionId,
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.LIVE_FEED.id,
                    optionsJson = LiveFeedRequestBody(
                        kind = NostrEventKind.LiveActivity.value,
                        pubkey = streamingNaddr.userId,
                        identifier = streamingNaddr.identifier,
                        userPubkey = userId,
                        contentModerationMode = contentModerationMode,
                    ).encodeToJsonString(),
                ),
                inactivityTimeout = 500.milliseconds,
            )
            .map {
                LiveFeedResponse(
                    zaps = it.filterNostrEvents(NostrEventKind.Zap),
                    chatMessages = it.filterNostrEvents(NostrEventKind.ChatMessage),
                )
            }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun subscribeToLiveEventsFromFollows(userId: String): Flow<NostrEvent> {
        val subscriptionId = Uuid.random().toPrimalSubscriptionId()
        return primalApiClient
            .subscribe(
                subscriptionId = subscriptionId,
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.LIVE_EVENTS_FROM_FOLLOWS.id,
                    optionsJson = LiveEventsFromFollowsRequest(pubkey = userId).encodeToJsonString(),
                ),
            ).catch { Napier.w(throwable = it) { "Couldn't subscribe to `live_events_from_follows`." } }
            .mapNotNull { message ->
                when (message) {
                    is NostrIncomingMessage.EventMessage -> message.nostrEvent
                    else -> null
                }?.takeIf { it.kind == NostrEventKind.LiveActivity.value }
            }
    }

    override suspend fun findLiveStream(body: FindLiveStreamRequestBody): FindLiveStreamResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.FIND_LIVE_EVENTS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return FindLiveStreamResponse(
            liveActivity = queryResult.findNostrEvent(NostrEventKind.LiveActivity),
        )
    }
}
