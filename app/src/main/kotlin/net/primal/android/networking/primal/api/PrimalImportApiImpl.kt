package net.primal.android.networking.primal.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonArray
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.primal.api.model.ImportRequestBody
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.model.NostrEvent

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
