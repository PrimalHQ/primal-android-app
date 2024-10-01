package net.primal.android.articles.db

import androidx.room.Entity
import net.primal.android.attachments.domain.CdnImage

@Entity(primaryKeys = ["articleId", "authorId"])
data class ArticleData(
    val eventId: String,
    val articleId: String,
    val authorId: String,
    val createdAt: Long,
    val content: String,
    val title: String,
    val publishedAt: Long,
    val raw: String,
    val imageCdnImage: CdnImage? = null,
    val summary: String? = null,
    val authorMetadataId: String? = null,
    val wordsCount: Int? = null,
    val uris: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
)
