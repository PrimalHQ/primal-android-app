package net.primal.data.local.dao.events

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventStats(
    @PrimaryKey
    val eventId: String,
    val likes: Long = 0,
    val replies: Long = 0,
    val mentions: Long = 0,
    val reposts: Long = 0,
    val zaps: Long = 0,
    val satsZapped: Long = 0,
    val score: Long = 0,
    val score24h: Long = 0,
)
