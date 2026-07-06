package net.primal.data.local.dao.mutes

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class MutedUser(
    @Embedded
    val mutedAccount: MutedItemData,

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["item"],
    )
    val profileData: ProfileData? = null,
)
