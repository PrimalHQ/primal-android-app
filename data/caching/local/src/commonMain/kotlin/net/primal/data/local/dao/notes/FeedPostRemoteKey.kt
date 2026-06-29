package net.primal.data.local.dao.notes

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["ownerId", "eventId", "directive"],
    indices = [
        Index(value = ["ownerId", "directive", "cachedAt"]),
    ],
)
data class FeedPostRemoteKey(
    val ownerId: String,
    val eventId: String,
    val directive: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
