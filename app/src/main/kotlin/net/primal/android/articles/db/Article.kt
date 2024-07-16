package net.primal.android.articles.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileData

data class Article(
    @Embedded
    val data: ArticleData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileData? = null,
)
