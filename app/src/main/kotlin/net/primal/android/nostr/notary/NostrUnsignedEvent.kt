package net.primal.android.nostr.notary

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class NostrUnsignedEvent(
    @SerialName("pubkey") val pubKey: String,
    @SerialName("created_at") val createdAt: Long = Instant.now().epochSecond,
    val tags: List<JsonArray> = emptyList(),
    val kind: Int,
    val content: String,
)
