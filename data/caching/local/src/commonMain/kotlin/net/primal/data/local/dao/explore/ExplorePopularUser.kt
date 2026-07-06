package net.primal.data.local.dao.explore

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.streams.StreamData

data class ExplorePopularUser(
    @Embedded
    val data: ExplorePopularUserCrossRef,

    @Relation(
        parentColumns = ["profileId"],
        entityColumns = ["ownerId"],
    )
    val profile: ProfileData?,

    @Relation(
        parentColumns = ["profileId"],
        entityColumns = ["mainHostId"],
    )
    val streams: List<StreamData> = emptyList(),
)
