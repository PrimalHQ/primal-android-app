package net.primal.data.local.dao.explore

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.streams.StreamData

data class ExplorePopularUser(
    @Embedded
    val data: ExplorePopularUserCrossRef,

    @Relation(
        parentColumn = "profileId",
        entityColumn = "ownerId",
    )
    val profile: ProfileData?,

    @Relation(
        parentColumn = "profileId",
        entityColumn = "mainHostId",
    )
    val streams: List<StreamData> = emptyList(),
)
