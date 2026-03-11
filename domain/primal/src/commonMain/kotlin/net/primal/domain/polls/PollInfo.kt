package net.primal.domain.polls

data class PollInfo(
    val postId: String,
    val authorId: String,
    val zapRecipientId: String? = null,
    val isZapPoll: Boolean,
    val endsAt: Long? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
    val options: List<PollOptionInfo>,
)
