package net.primal.data.local.dao.profiles

import androidx.room.Embedded
import androidx.room.Relation

data class MutedUser(
    @Embedded
    val mutedAccount: MutedUserData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "userId",
    )
    val profileData: ProfileData? = null,
)
