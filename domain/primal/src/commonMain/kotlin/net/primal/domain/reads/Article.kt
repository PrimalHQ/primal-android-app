package net.primal.domain.reads

import net.primal.domain.bookmarks.PublicBookmark
import net.primal.domain.events.EventZap
import net.primal.domain.events.NostrEventStats
import net.primal.domain.events.NostrEventUserStats
import net.primal.domain.links.CdnImage
import net.primal.domain.profile.ProfileData

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
    val userEventStats: NostrEventUserStats? = null,
    val eventZaps: List<EventZap> = emptyList(),
    val bookmark: PublicBookmark? = null,
    val highlights: List<Highlight> = emptyList(),
)
