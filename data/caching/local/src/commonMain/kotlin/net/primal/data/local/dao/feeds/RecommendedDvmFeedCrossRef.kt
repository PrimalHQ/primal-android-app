package net.primal.data.local.dao.feeds

import androidx.room.Entity

@Entity(primaryKeys = ["ownerId", "dvmEventId", "specKindFilter"])
data class RecommendedDvmFeedCrossRef(
    val ownerId: String,
    val dvmEventId: String,
    val specKindFilter: String,
    val position: Int,
)
