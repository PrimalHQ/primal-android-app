package net.primal.data.remote.api.broadcast

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.broadcast.model.BroadcastEventResponse
import net.primal.data.remote.api.broadcast.model.BroadcastRequestBody
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

internal class BroadcastApiImpl(
    private val primalApiClient: PrimalApiClient,
) : BroadcastApi {

    override suspend fun broadcastEvents(
        events: List<NostrEvent>,
        relays: List<String>,
    ): List<BroadcastEventResponse> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.BROADCAST_EVENTS.id,
                optionsJson = BroadcastRequestBody(
                    events = events,
                    relays = relays,
                ).encodeToJsonString(),
            ),
        )

        val broadcastEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalBroadcastResult)
        val responses = broadcastEvent?.content.decodeFromJsonStringOrNull<List<BroadcastEventResponse>>()

        return responses ?: throw NetworkException(
            message = "Primal NostrEvent ${NostrEventKind.PrimalBroadcastResult.value} not found or invalid.",
        )
    }
}
