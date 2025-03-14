package net.primal.data.local.dao.messages

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr

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
