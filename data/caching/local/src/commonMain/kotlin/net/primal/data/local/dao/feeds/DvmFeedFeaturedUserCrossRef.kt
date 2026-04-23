package net.primal.data.local.dao.feeds

import androidx.room.Entity

@Entity(primaryKeys = ["ownerId", "dvmEventId", "profileId"])
data class DvmFeedFeaturedUserCrossRef(
    val ownerId: String,
    val dvmEventId: String,
    val profileId: String,
    val position: Int,
)
