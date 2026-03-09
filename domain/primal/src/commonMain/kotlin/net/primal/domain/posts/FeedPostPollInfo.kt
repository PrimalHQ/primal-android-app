package net.primal.domain.posts

data class FeedPostPollInfo(
    val pollType: PollType,
    val options: List<PollOptionInfo>,
    val endsAt: Long? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
) {
    enum class PollType { User, Zap }

    data class PollOptionInfo(
        val id: String,
        val label: String,
        val voteCount: Int = 0,
        val satsZapped: Long = 0,
    )
}
