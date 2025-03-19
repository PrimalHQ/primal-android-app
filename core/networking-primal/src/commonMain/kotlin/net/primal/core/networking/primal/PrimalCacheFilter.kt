package net.primal.core.networking.primal

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.serialization.SocketsJson

data class PrimalCacheFilter(
    val primalVerb: String? = null,
    val optionsJson: String? = null,
) {
    fun toPrimalJsonObject() =
        buildJsonObject {
            put(
                "cache",
                buildJsonArray {
                    add(primalVerb)
                    if (optionsJson != null) {
                        add(SocketsJson.decodeFromString(optionsJson))
                    }
                },
            )
        }
}
