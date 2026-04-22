package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExploreFollowPackData(
    @PrimaryKey val aTag: String,
    val position: Int,
)
