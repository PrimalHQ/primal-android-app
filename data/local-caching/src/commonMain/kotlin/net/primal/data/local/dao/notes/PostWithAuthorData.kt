package net.primal.data.local.dao.notes

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class PostWithAuthorData(
    @Embedded
    val post: PostData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData?,
)
