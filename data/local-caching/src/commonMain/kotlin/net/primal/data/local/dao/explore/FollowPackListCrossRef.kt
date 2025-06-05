package net.primal.data.local.dao.explore

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["followPackATag"], unique = true),
    ],
)
data class FollowPackListCrossRef(
    @PrimaryKey(autoGenerate = true)
    val position: Long = 0,
    val followPackATag: String,
)
