package net.primal.data.local.dao.polls

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["postId"]),
        Index(value = ["voterId"]),
        Index(value = ["postId", "optionId", "createdAt"]),
    ],
)
data class PollVoteData(
    @PrimaryKey
    val eventId: String,
    val postId: String,
    val optionId: String,
    val voterId: String,
    val amountInSats: Long? = null,
    val zapComment: String? = null,
    val createdAt: Long,
)
