package net.primal.data.remote.api.importing

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.importing.model.ImportRequestBody
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonArray
import net.primal.domain.publisher.NostrEventImporter

internal class PrimalImportApiImpl(
    private val primalApiClient: PrimalApiClient,
) : PrimalImportApi, NostrEventImporter {

    override suspend fun importEvents(events: List<NostrEvent>): Boolean {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.IMPORT_EVENTS.id,
                optionsJson = ImportRequestBody(nostrEvents = events.toNostrJsonArray()).encodeToJsonString(),
            ),
        )

        return when (result.terminationMessage) {
            is NostrIncomingMessage.EoseMessage -> true
            else -> false
        }
    }
}
