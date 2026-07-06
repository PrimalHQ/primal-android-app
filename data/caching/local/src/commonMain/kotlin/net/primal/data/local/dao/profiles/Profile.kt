package net.primal.data.local.dao.profiles

import androidx.room3.Embedded
import androidx.room3.Relation

data class Profile(

    @Embedded
    val metadata: ProfileData? = null,

    @Relation(
        entityColumns = ["profileId"],
        parentColumns = ["ownerId"],
    )
    val stats: ProfileStats? = null,
)
