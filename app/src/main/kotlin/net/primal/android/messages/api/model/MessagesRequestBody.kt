package net.primal.android.messages.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesRequestBody(
    @SerialName("receiver") val userId: String,
    @SerialName("sender") val participantId: String,
    val limit: Int? = null,
    val until: Long? = null,
    val since: Long? = null,
)
