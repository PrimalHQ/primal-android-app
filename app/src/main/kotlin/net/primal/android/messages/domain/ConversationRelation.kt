package net.primal.android.messages.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ConversationRelation {
    @SerialName("follows")
    Follows,

    @SerialName("other")
    Other,
}
