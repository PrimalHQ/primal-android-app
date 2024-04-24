package net.primal.android.note.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteActionsRequestBody(
    @SerialName("event_id") val postId: String,
    @SerialName("kind") val kind: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("offset") val offset: Int = 0,
)
