package net.primal.domain.nostr

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class NostrUnsignedEvent(
    @SerialName("pubkey") val pubKey: String,
    @SerialName("created_at") val createdAt: Long = Clock.System.now().epochSeconds,
    val tags: List<JsonArray> = emptyList(),
    val kind: Int,
    val content: String,
)
