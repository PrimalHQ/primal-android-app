package net.primal.data.remote.api.importing

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalHttpApiClient
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.importing.model.ImportEventsResponse
import net.primal.data.remote.api.importing.model.ImportRequestBody
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.serialization.toNostrJsonArray

internal class PrimalImportApiImpl(
    private val primalHttpApiClient: PrimalHttpApiClient,
) : PrimalImportApi {

    override suspend fun importEvents(events: List<NostrEvent>): Boolean {
        val response = primalHttpApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.IMPORT_EVENTS.id,
                optionsJson = ImportRequestBody(nostrEvents = events.toNostrJsonArray()).encodeToJsonString(),
            ),
        )

        val result = response.resolveImportEventsResponse()
        return result != null && result.errors == 0
    }
}

/**
 * Returns `null` when the server answered with an error object instead of the expected array of
 * result events.
 */
internal fun JsonElement.resolveImportEventsResponse(): ImportEventsResponse? =
    (this as? JsonArray)
        ?.filterIsInstance<JsonObject>()
        ?.firstOrNull {
            (it["kind"] as? JsonPrimitive)?.intOrNull == NostrEventKind.PrimalImportEventsResult.value
        }
        ?.let { (it["content"] as? JsonPrimitive)?.content }
        .decodeFromJsonStringOrNull()
