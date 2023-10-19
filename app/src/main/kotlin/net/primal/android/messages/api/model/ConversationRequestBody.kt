package net.primal.android.messages.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.messages.domain.ConversationRelation

@Serializable
data class ConversationRequestBody(
    @SerialName("user_pubkey") val userId: String,
    val relation: ConversationRelation,
)
