package net.primal.domain.posts

import net.primal.domain.polls.PollOptionInfo

data class FeedPostPollInfo(
    val pollType: PollType,
    val options: List<PollOptionInfo>,
    val endsAt: Long? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
) {
    enum class PollType { User, Zap }
}
