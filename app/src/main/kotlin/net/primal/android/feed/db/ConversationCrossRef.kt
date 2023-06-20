package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "postId",
        "replyPostId"
    ],
    indices = [
        Index(value = ["postId"]),
        Index(value = ["replyPostId"])
    ]
)
data class ConversationCrossRef(
    val postId: String,
    val replyPostId: String,
)
