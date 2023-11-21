package net.primal.android.messages.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.profile.db.ProfileData

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
    val lastMessage: DirectMessageData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "lastMessageId",
    )
    val lastMessageNoteAttachments: List<NoteAttachment>,

    @Relation(
        entityColumn = "postId",
        parentColumn = "lastMessageId",
    )
    val lastMessageNostrUris: List<NoteNostrUri>,
)
