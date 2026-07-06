package net.primal.data.local.dao.explore

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
