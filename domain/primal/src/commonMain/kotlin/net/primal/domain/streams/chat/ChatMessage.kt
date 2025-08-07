package net.primal.domain.streams.chat

import net.primal.domain.profile.ProfileData

data class ChatMessage(
    val messageId: String,
    val author: ProfileData,
    val content: String,
    val createdAt: Long,
    val raw: String,
    val client: String?,
)
