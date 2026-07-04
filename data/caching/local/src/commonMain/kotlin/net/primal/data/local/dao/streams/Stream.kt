package net.primal.data.local.dao.streams

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.profiles.ProfileData

data class Stream(
    @Embedded
    val data: StreamData,

    @Relation(
        parentColumns = ["mainHostId"],
        entityColumns = ["ownerId"],
    )
    val mainHost: ProfileData? = null,

    @Relation(
        parentColumns = ["aTag"],
        entityColumns = ["eventId"],
    )
    val stats: EventStats? = null,
)
