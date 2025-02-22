package net.primal.android.articles.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["ownerId", "spec", "articleATag", "articleAuthorId"], unique = true),
    ],
)
data class ArticleFeedCrossRef(
    @PrimaryKey(autoGenerate = true)
    val position: Long = 0,
    val ownerId: String,
    val spec: String,
    val articleATag: String,
    val articleAuthorId: String,
)
