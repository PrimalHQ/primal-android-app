package net.primal.data.local.dao.polls

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class PollVoteWithProfile(
    @Embedded
    val vote: PollVoteData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "voterId",
    )
    val profile: ProfileData? = null,
)
