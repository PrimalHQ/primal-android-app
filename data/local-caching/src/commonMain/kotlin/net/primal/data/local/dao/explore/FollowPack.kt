package net.primal.data.local.dao.explore

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.profiles.ProfileStats

data class FollowPack(
    @Embedded
    val data: FollowPackData,

    @Relation(
        parentColumn = "authorId",
        entityColumn = "ownerId",
    )
    val author: ProfileData? = null,

    @Relation(
        parentColumn = "authorId",
        entityColumn = "profileId",
    )
    val authorStats: ProfileStats?,

    @Relation(
        parentColumn = "aTag",
        entityColumn = "ownerId",
        associateBy = Junction(
            value = FollowPackProfileCrossRef::class,
            parentColumn = "followPackATag",
            entityColumn = "profileId",
        ),
    )
    val people: List<ProfileData> = emptyList(),

    @Relation(
        parentColumn = "aTag",
        entityColumn = "profileId",
        associateBy = Junction(
            value = FollowPackProfileCrossRef::class,
            parentColumn = "followPackATag",
            entityColumn = "profileId",
        ),
    )
    val stats: List<ProfileStats> = emptyList(),
)
