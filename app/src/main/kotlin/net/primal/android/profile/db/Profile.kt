package net.primal.android.profile.db

import androidx.room.Embedded
import androidx.room.Relation

data class Profile(
    @Embedded
    val metadata: ProfileData? = null,

    @Relation(
        entityColumn = "profileId",
        parentColumn = "ownerId",
    )
    val stats: ProfileStats? = null,
)
