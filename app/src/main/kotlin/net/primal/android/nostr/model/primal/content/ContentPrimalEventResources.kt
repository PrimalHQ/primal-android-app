package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalResourceVariant

@Serializable
data class ContentPrimalEventResources(
    @SerialName("event_id") val eventId: String,
    val resources: List<EventResource>,
)

@Serializable
data class EventResource(
    val url: String,
    @SerialName("mt") val mimeType: String?,
    val variants: List<PrimalResourceVariant>
)
