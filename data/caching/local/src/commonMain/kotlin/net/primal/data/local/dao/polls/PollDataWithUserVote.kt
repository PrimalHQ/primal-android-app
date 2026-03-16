package net.primal.data.local.dao.polls

import androidx.room.Embedded

data class PollDataWithUserVote(
    @Embedded val pollData: PollData,
    val userVotedForOption: String? = null,
)
