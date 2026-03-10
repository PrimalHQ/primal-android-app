package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.polls.PollData
import net.primal.data.local.dao.polls.PollType
import net.primal.domain.posts.FeedPostPollInfo

fun PollData.asFeedPostPollInfo(): FeedPostPollInfo {
    return FeedPostPollInfo(
        pollType = when (this.pollType) {
            PollType.User -> FeedPostPollInfo.PollType.User
            PollType.Zap -> FeedPostPollInfo.PollType.Zap
        },
        options = this.options.map { option ->
            FeedPostPollInfo.PollOptionInfo(
                id = option.id,
                label = option.label,
                voteCount = option.voteCount,
                satsZapped = option.satsZapped,
            )
        },
        endsAt = this.endsAt,
        valueMinimum = this.valueMinimum,
        valueMaximum = this.valueMaximum,
    )
}
