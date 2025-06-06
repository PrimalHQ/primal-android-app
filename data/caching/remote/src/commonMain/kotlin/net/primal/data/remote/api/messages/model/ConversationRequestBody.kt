package net.primal.data.remote.api.messages.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.messages.ConversationRelation

@Serializable
data class ConversationRequestBody(
    @SerialName("user_pubkey") val userId: String,
    val relation: ConversationRelation,
)
