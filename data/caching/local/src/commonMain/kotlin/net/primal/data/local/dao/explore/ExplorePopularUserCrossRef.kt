package net.primal.data.local.dao.explore

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class ExplorePopularUserCrossRef(
    @PrimaryKey val profileId: String,
    val position: Int,
    val score: Float?,
)
