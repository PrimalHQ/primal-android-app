package net.primal.data.local.dao.polls

import androidx.room3.Embedded
import androidx.room3.Relation

data class Poll(

    @Embedded
    val data: PollData,

    @Relation(
        entityColumns = ["postId"],
        parentColumns = ["postId"],
    )
    val votes: List<PollVoteData> = emptyList(),
)
