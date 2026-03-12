package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.polls.PollData
import net.primal.data.local.dao.polls.asDO
import net.primal.domain.polls.PollInfo
import net.primal.domain.polls.PollOptionInfo

fun PollData.asPollInfo(): PollInfo {
    return PollInfo(
        postId = this.postId,
        authorId = this.authorId,
        zapRecipientId = this.zapRecipientId,
        pollType = this.pollType.asDO(),
        endsAt = this.endsAt,
        valueMinimum = this.valueMinimum,
        valueMaximum = this.valueMaximum,
        options = this.options.map { option ->
            PollOptionInfo(
                id = option.id,
                label = option.label,
                voteCount = option.voteCount,
                satsZapped = option.satsZapped,
            )
        },
    )
}
