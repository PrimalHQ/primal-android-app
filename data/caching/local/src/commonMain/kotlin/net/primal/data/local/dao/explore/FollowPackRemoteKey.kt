package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FollowPackRemoteKey(
    @PrimaryKey
    val followPackATag: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
