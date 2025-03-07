package net.primal.networking.primal.api

import net.primal.networking.model.NostrEvent
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalCacheFilter
import net.primal.networking.primal.PrimalVerb
import net.primal.networking.primal.api.model.ImportRequestBody
import net.primal.networking.sockets.NostrIncomingMessage
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.toJsonArray

internal class PrimalImportApiImpl(
    private val primalApiClient: PrimalApiClient,
) : PrimalImportApi {

    override suspend fun importEvents(events: List<NostrEvent>): Boolean {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.IMPORT_EVENTS,
                optionsJson = NostrJson.encodeToString(
                    ImportRequestBody(nostrEvents = events.toJsonArray()),
                ),
            ),
        )

        return when (result.terminationMessage) {
            is NostrIncomingMessage.EoseMessage -> true
            else -> false
        }
    }
}
