package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["timestamp", "feedDirective"],
)
data class FeedPostSync(
    val timestamp: Long,
    val feedDirective: String,
    val count: Int,
    val postIds: List<String>,
)
