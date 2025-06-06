package net.primal.data.local.dao.explore

import androidx.room.Entity

@Entity(primaryKeys = ["followPackATag", "profileId"])
data class FollowPackProfileCrossRef(
    val followPackATag: String,
    val profileId: String,
)
