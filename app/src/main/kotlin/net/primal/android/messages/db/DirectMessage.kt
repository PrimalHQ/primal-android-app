package net.primal.android.messages.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri

data class DirectMessage(

    @Embedded
    val data: DirectMessageData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "messageId",
    )
    val attachments: List<NoteAttachment>,

    @Relation(
        entityColumn = "postId",
        parentColumn = "messageId",
    )
    val nostrUris: List<NoteNostrUri>,
)
