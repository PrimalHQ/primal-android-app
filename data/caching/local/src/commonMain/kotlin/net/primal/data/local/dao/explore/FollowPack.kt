package net.primal.data.local.dao.explore

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.profiles.ProfileStats

data class FollowPack(
    @Embedded
    val data: FollowPackData,

    @Relation(
        parentColumns = ["authorId"],
        entityColumns = ["ownerId"],
    )
    val author: ProfileData? = null,

    @Relation(
        parentColumns = ["authorId"],
        entityColumns = ["profileId"],
    )
    val authorStats: ProfileStats?,

    @Relation(
        parentColumns = ["aTag"],
        entityColumns = ["ownerId"],
        associateBy = Junction(
            value = FollowPackProfileCrossRef::class,
            parentColumns = ["followPackATag"],
            entityColumns = ["profileId"],
        ),
    )
    val people: List<ProfileData> = emptyList(),

    @Relation(
        parentColumns = ["aTag"],
        entityColumns = ["profileId"],
        associateBy = Junction(
            value = FollowPackProfileCrossRef::class,
            parentColumns = ["followPackATag"],
            entityColumns = ["profileId"],
        ),
    )
    val stats: List<ProfileStats> = emptyList(),
)
