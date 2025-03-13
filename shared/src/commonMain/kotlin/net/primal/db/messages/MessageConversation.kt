package net.primal.db.messages

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.db.events.EventUri
import net.primal.db.events.EventUriNostr
import net.primal.db.profiles.ProfileData

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
