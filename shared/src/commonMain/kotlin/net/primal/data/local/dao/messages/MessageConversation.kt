package net.primal.data.local.dao.messages

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.profiles.ProfileData

data class MessageConversation(
    @Embedded
    val data: MessageConversationData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "participantId",
    )
    val participant: ProfileData?,

    @Relation(
        entityColumn = "messageId",
        parentColumn = "lastMessageId",
    )
    val lastMessage: DirectMessageData?,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "lastMessageId",
    )
    val lastMessageUris: List<EventUri>,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "lastMessageId",
    )
    val lastMessageNostrUris: List<EventUriNostr>,
)
