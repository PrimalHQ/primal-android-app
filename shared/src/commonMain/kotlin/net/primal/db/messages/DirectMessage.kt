package net.primal.db.messages

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.db.events.EventUri
import net.primal.db.events.EventUriNostr

data class DirectMessage(

    @Embedded
    val data: DirectMessageData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "messageId",
    )
    val eventUris: List<EventUri>,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "messageId",
    )
    val eventNostrUris: List<EventUriNostr>,
)
