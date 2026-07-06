package net.primal.data.local.dao.streams

import androidx.room3.Entity

@Entity(primaryKeys = ["streamATag", "ownerId"])
data class StreamFollowsCrossRef(
    val streamATag: String,
    val ownerId: String,
)
