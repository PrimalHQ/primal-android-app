package net.primal.db.notes

import androidx.room.Entity

@Entity(
    primaryKeys = ["ownerId", "eventId", "directive"],
)
data class FeedPostRemoteKey(
    val ownerId: String,
    val eventId: String,
    val directive: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
