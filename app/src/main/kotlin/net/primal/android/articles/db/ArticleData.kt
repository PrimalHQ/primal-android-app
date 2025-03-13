package net.primal.android.articles.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.events.domain.CdnImage

@Entity
data class ArticleData(
    @PrimaryKey
    val aTag: String,
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
