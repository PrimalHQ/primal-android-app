package net.primal.networking.primal.upload.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class UploadCompleteRequest(
    @SerialName("event_from_user") val event: NostrEvent,
)
