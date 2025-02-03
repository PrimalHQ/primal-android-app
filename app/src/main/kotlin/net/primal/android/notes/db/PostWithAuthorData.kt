package net.primal.android.notes.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileData

data class PostWithAuthorData(
    @Embedded
    val post: PostData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData?,
)
