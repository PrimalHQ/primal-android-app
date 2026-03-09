package net.primal.android.events.polls.votes.model

data class PollOptionUi(
    val id: String,
    val title: String,
    val voteCount: Int,
    val totalSats: Long = 0,
    val voters: List<PollVoterUi>,
)
