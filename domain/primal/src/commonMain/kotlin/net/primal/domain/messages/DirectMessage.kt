package net.primal.domain.messages

import net.primal.domain.links.EventLink
import net.primal.domain.links.EventUriNostrReference

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
