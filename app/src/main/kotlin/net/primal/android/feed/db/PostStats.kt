package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostStats(
    @PrimaryKey
    val postId: String,
    val likes: Long,
    val replies: Long,
    val mentions: Long,
    val reposts: Long,
    val zaps: Long,
    val satsZapped: Long,
    val score: Long,
    val score24h: Long,
)
