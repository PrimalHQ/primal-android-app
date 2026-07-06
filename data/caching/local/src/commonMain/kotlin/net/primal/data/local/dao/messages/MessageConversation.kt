package net.primal.data.local.dao.messages

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.profiles.ProfileData

data class MessageConversation(
    @Embedded
    val data: MessageConversationData,

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["participantId"],
    )
    val participant: ProfileData?,

    @Relation(
        entityColumns = ["messageId"],
        parentColumns = ["lastMessageId"],
    )
    val lastMessage: DirectMessageData?,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["lastMessageId"],
    )
    val lastMessageUris: List<EventUri>,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["lastMessageId"],
    )
    val lastMessageNostrUris: List<EventUriNostr>,
)
