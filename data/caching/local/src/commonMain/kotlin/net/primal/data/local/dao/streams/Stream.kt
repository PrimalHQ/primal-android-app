package net.primal.data.local.dao.streams

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData

data class Stream(
    @Embedded
    val data: StreamData,

    @Relation(
        parentColumn = "mainHostId",
        entityColumn = "ownerId",
    )
    val mainHost: ProfileData? = null,

    @Relation(
        parentColumn = "aTag",
        entityColumn = "eventId",
    )
    val stats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "aTag")
    val eventZaps: List<EventZap> = emptyList(),
)
