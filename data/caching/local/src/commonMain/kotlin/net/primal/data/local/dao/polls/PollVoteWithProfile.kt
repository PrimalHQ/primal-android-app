package net.primal.data.local.dao.polls

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class PollVoteWithProfile(
    @Embedded
    val vote: PollVoteData,

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["voterId"],
    )
    val profile: ProfileData? = null,
)
