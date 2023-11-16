package net.primal.android.thread.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "postId",
        "replyPostId",
    ],
    indices = [
        Index(value = ["postId"]),
        Index(value = ["replyPostId"]),
    ],
)
data class ThreadConversationCrossRef(
    val postId: String,
    val replyPostId: String,
)
