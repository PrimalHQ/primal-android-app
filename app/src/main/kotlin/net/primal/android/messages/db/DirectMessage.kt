package net.primal.android.messages.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr

data class DirectMessage(

    @Embedded
    val data: DirectMessageData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "messageId",
    )
    val eventUris: List<EventUri>,

    @Relation(
        entityColumn = "noteId",
        parentColumn = "messageId",
    )
    val eventNostrUris: List<EventUriNostr>,
)
