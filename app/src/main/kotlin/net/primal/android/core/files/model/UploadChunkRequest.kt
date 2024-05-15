package net.primal.android.core.files.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class UploadChunkRequest(
    @SerialName("event_from_user") val event: NostrEvent,
)
