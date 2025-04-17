package net.primal.data.local.dao.mutes

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class MutedUser(
    @Embedded
    val mutedAccount: MutedItemData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "item",
    )
    val profileData: ProfileData? = null,
)
