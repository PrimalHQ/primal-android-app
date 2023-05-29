package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["feedDirective", "postId"],
)
data class FeedPostDataCrossRef(
    val feedDirective: String,
    val postId: String,
)
