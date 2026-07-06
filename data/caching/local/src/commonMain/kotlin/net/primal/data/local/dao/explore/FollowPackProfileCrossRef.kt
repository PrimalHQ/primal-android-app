package net.primal.data.local.dao.explore

import androidx.room3.Entity
import androidx.room3.Index

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
