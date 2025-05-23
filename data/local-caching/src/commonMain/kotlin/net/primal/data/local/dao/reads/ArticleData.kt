package net.primal.data.local.dao.reads

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.links.CdnImage

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
    val client: String? = null,
)
