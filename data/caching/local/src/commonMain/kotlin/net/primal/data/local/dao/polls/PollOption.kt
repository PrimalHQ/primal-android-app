package net.primal.data.local.dao.polls

import kotlinx.serialization.Serializable

@Serializable
data class PollOption(
    val id: String,
    val label: String,
    val voteCount: Int = 0,
    val satsZapped: Long = 0,
)
