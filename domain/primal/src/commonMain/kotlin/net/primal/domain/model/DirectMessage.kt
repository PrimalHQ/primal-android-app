package net.primal.domain.model

import net.primal.domain.EventLink
import net.primal.domain.EventUriNostrReference

data class DirectMessage(
    val messageId: String,
    val ownerId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    val content: String,
    val hashtags: List<String>,
    val links: List<EventLink> = emptyList(),
    val nostrUris: List<EventUriNostrReference> = emptyList(),
)
