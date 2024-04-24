package net.primal.android.note.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteZapsRequestBody(
    @SerialName("event_id") val noteId: String,
    @SerialName("user_pubkey") val userId: String,
    @SerialName("limit") val limit: Int,
    @SerialName("offset") val offset: Int = 0,
)
