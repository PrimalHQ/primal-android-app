package net.primal.domain.model

import net.primal.domain.CdnImage
import net.primal.domain.EventZap

data class Article(
    val aTag: String,
    val eventId: String,
    val articleId: String,
    val authorId: String,
    val createdAt: Long,
    val content: String,
    val title: String,
    val publishedAt: Long,
    val articleRawJson: String,
    val imageCdnImage: CdnImage? = null,
    val summary: String? = null,
    val authorMetadataId: String? = null,
    val wordsCount: Int? = null,
    val uris: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val author: ProfileData? = null,
    val eventStats: NostrEventStats? = null,
    val userEventStats: NostrEventStats? = null,
    val eventZaps: List<EventZap> = emptyList(),
    val bookmark: PublicBookmark? = null,
    val highlights: List<Highlight> = emptyList(),
)
