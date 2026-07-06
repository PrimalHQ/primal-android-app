package net.primal.data.local.dao.messages

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr

data class DirectMessage(

    @Embedded
    val data: DirectMessageData,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["messageId"],
    )
    val eventUris: List<EventUri>,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["messageId"],
    )
    val eventNostrUris: List<EventUriNostr>,
)
