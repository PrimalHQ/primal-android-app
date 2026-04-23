package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExplorePopularUserCrossRef(
    @PrimaryKey val profileId: String,
    val position: Int,
    val score: Float?,
)
