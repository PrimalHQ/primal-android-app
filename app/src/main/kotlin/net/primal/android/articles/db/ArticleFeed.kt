package net.primal.android.articles.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ArticleFeed(
    @PrimaryKey
    val spec: String,
    val name: String,
)
