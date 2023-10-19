package net.primal.android.messages.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MessagesRequestBody(
    val receiver: String,
    val sender: String,
    val notes: String? = null,
    val limit: Int? = null,
    val until: Long? = null,
    val since: Long? = null,
)
