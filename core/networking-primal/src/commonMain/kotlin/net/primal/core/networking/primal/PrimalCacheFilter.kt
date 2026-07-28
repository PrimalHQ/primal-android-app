package net.primal.core.networking.primal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.serialization.SocketsJson

data class PrimalCacheFilter(
    val primalVerb: String? = null,
    val optionsJson: String? = null,
) {
    /**
     * Positional `[verb, options]` request, as expected by the HTTP api.
     */
    fun toPrimalJsonArray(): JsonArray =
        buildJsonArray {
            add(primalVerb)
            if (optionsJson != null) {
                add(SocketsJson.decodeFromString<JsonElement>(optionsJson))
            }
        }

    /**
     * The same request wrapped in a `cache` envelope, as expected by the socket api.
     */
    fun toPrimalJsonObject(): JsonObject =
        buildJsonObject {
            put("cache", toPrimalJsonArray())
        }
}
