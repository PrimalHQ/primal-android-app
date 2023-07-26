package net.primal.android.nostr.notary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import java.time.Instant

@Serializable
data class NostrUnsignedEvent(
    @SerialName("pubkey") val pubKey: String,
    @SerialName("created_at") val createdAt: Long = Instant.now().epochSecond,
    val kind: Int,
    val tags: List<JsonArray>? = null,
    val content: String,
)
