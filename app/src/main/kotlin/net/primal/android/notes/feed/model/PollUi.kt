package net.primal.android.notes.feed.model

import java.time.Instant
import net.primal.domain.polls.PollType as DomainPollType

enum class PollType {
    User,
    Zap,
}

fun DomainPollType.asUiModel(): PollType =
    when (this) {
        DomainPollType.User -> PollType.User
        DomainPollType.Zap -> PollType.Zap
    }

enum class PollState {
    Pending,
    Voted,
    Ended,
}

data class PollOptionUi(
    val id: String,
    val label: String,
    val voteCount: Int = 0,
    val votePercentage: Float = 0f,
    val satsZapped: Long = 0,
    val isWinner: Boolean = false,
)

data class PollUi(
    val authorId: String = "",
    val zapRecipientId: String? = null,
    val pollType: PollType = PollType.User,
    val options: List<PollOptionUi>,
    val endsAt: Instant? = null,
    val state: PollState,
    val userVotedOptionId: String? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
) {
    val totalVotes: Int get() = options.sumOf { it.voteCount }
}
