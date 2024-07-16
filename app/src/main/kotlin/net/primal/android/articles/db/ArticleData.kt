package net.primal.android.articles.db

import androidx.room.Entity

@Entity
data class ArticleData(
    val articleId: String,
    val authorId: String,
    val title: String,
    val publishedAt: Long,
    val raw: String,
    val image: String? = null,
    val summary: String? = null,
    val authorMetadataId: String? = null,
    val wordsCount: Int? = null,
)
