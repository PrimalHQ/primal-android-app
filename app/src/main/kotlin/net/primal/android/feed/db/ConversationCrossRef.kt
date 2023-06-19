package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["postId", "replyPostId"],
)
data class ConversationCrossRef(
    val postId: String,
    val replyPostId: String,
)
