package net.primal.data.local.dao.polls

import androidx.room.Entity

@Entity(primaryKeys = ["postId", "optionId", "eventId"])
data class PollVoterRemoteKey(
    val postId: String,
    val optionId: String,
    val eventId: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
