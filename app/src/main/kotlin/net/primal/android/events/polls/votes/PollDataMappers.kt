package net.primal.android.events.polls.votes

import java.time.Instant
import net.primal.android.notes.feed.model.PollOptionUi
import net.primal.android.notes.feed.model.PollState
import net.primal.android.notes.feed.model.PollType
import net.primal.android.notes.feed.model.PollUi
import net.primal.domain.polls.PollInfo

internal fun PollInfo.asPollUi(userVotedOptionIds: Set<String> = emptySet()): PollUi {
    val endsAtInstant = endsAt?.let { Instant.ofEpochSecond(it) }
    val totalVotes = options.sumOf { it.voteCount }.coerceAtLeast(1)
    val totalSats = options.sumOf { it.satsZapped }.coerceAtLeast(1)
    val pollType = if (isZapPoll) PollType.Zap else PollType.User

    val state = when {
        endsAtInstant != null && endsAtInstant < Instant.now() -> PollState.Ended
        userVotedOptionIds.isNotEmpty() -> PollState.Voted
        else -> PollState.Pending
    }

    val winner = when (pollType) {
        PollType.User -> options.maxBy { it.voteCount }.takeIf { it.voteCount != 0 }
        PollType.Zap -> options.maxBy { it.satsZapped }.takeIf { it.satsZapped != 0L }
    }

    return PollUi(
        authorId = authorId,
        zapRecipientId = zapRecipientId,
        pollType = pollType,
        options = options.map { option ->
            PollOptionUi(
                id = option.id,
                label = option.label,
                voteCount = option.voteCount,
                satsZapped = option.satsZapped,
                votePercentage = when (pollType) {
                    PollType.User -> option.voteCount.toFloat() / totalVotes
                    PollType.Zap -> option.satsZapped.toFloat() / totalSats
                },
                isWinner = state == PollState.Ended && winner == option,
            )
        },
        endsAt = endsAtInstant,
        state = state,
        selectedOptionIds = userVotedOptionIds,
        valueMinimum = valueMinimum,
        valueMaximum = valueMaximum,
    )
}
