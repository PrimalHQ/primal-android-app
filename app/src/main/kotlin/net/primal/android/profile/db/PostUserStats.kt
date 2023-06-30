package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "postId",
        "userId"
    ],
    indices = [
        Index(value = ["postId"]),
        Index(value = ["userId"]),
    ],
)
data class PostUserStats(
    val postId: String,
    val userId: String,
    val replied: Boolean,
    val liked: Boolean,
    val reposted: Boolean,
    val zapped: Boolean,
)
