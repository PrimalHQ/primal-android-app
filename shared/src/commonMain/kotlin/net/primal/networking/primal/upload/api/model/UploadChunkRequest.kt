package net.primal.networking.primal.upload.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.networking.model.NostrEvent

@Serializable
data class UploadChunkRequest(
    @SerialName("event_from_user") val event: NostrEvent,
)
