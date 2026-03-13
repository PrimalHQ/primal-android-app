package net.primal.domain.polls

import kotlinx.serialization.Serializable

@Serializable
data class PollInfo(
    val postId: String,
    val authorId: String,
    val zapRecipientId: String? = null,
    val pollType: PollType,
    val endsAt: Long? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
    val options: List<PollOptionInfo>,
)
