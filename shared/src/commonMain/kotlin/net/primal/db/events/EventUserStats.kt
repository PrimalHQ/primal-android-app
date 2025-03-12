package net.primal.db.events

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "eventId",
        "userId",
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["userId"]),
    ],
)
data class EventUserStats(
    val eventId: String,
    val userId: String,
    val replied: Boolean = false,
    val liked: Boolean = false,
    val reposted: Boolean = false,
    val zapped: Boolean = false,
)
