package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["followPackATag", "profileId"],
    indices = [
        Index(value = ["profileId"]),
    ],
)
data class FollowPackProfileCrossRef(
    val followPackATag: String,
    val profileId: String,
)
