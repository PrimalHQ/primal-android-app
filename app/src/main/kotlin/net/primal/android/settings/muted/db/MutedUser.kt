package net.primal.android.settings.muted.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileData

data class MutedUser(
    @Embedded
    val mutedAccount: MutedUserData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "userId",
    )
    val profileData: ProfileData? = null,
)
