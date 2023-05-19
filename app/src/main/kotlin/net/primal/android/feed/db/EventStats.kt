package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventStats(
    @PrimaryKey
    val eventId: String,
    val likes: Int,
    val replies: Int,
    val mentions: Int,
    val reposts: Int,
    val zaps: Int,
    val satsZapped: Int,
    val score: Int,
    val score24h: Int,
)
