package net.primal.android.notes.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["eventId", "directive"],
)
data class FeedPostRemoteKey(
    val eventId: String,
    val directive: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
