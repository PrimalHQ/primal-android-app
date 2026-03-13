package net.primal.data.local.dao.polls

import androidx.room.Embedded
import androidx.room.Relation

data class Poll(

    @Embedded
    val data: PollData,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postId",
    )
    val votes: List<PollVoteData> = emptyList(),
)
