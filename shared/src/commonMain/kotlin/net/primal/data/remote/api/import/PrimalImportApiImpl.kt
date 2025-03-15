package net.primal.data.remote.api.import

import net.primal.data.remote.api.import.model.ImportRequestBody
import net.primal.data.serialization.NostrJson
import net.primal.data.serialization.toJsonArray
import net.primal.domain.nostr.NostrEvent
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalCacheFilter
import net.primal.data.remote.PrimalVerb
import net.primal.networking.sockets.NostrIncomingMessage

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
