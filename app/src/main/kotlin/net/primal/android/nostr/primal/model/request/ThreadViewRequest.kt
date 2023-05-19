package net.primal.android.nostr.primal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadViewRequest(
    @SerialName("event_id")  val eventId: String,
)
