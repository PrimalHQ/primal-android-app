package net.primal.android.notes.feed.model

import java.time.Instant

enum class PollType {
    Regular,
    Zap,
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
    val pollType: PollType = PollType.Regular,
    val options: List<PollOptionUi>,
    val totalVotes: Int,
    val endsAt: Instant? = null,
    val state: PollState,
    val selectedOptionIds: Set<String> = emptySet(),
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
)
