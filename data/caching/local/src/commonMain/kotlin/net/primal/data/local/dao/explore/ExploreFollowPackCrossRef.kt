package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExploreFollowPackCrossRef(
    @PrimaryKey val aTag: String,
    val position: Int,
)
