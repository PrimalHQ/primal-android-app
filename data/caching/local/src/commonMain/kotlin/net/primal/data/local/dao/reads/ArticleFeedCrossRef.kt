package net.primal.data.local.dao.reads

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["ownerId", "spec", "articleATag", "articleAuthorId"], unique = true),
        Index(value = ["ownerId", "spec", "position"]),
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
