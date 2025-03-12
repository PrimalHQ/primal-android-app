package net.primal.db.notes

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.db.profiles.ProfileData

data class PostWithAuthorData(
    @Embedded
    val post: PostData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData?,
)
