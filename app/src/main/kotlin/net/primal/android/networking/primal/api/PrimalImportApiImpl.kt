package net.primal.android.networking.primal.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonArray
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.api.model.ImportRequestBody
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEvent
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalCacheFilter
import net.primal.networking.sockets.NostrIncomingMessage

class PrimalImportApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
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
