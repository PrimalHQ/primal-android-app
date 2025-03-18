package net.primal.data.remote.api.upload.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
internal data class UploadChunkRequest(
    @SerialName("event_from_user") val event: NostrEvent,
)
