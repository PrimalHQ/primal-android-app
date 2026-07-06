package net.primal.data.local.dao.notes

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class PostWithAuthorData(
    @Embedded
    val post: PostData,

    @Relation(entityColumns = ["ownerId"], parentColumns = ["authorId"])
    val author: ProfileData?,
)
