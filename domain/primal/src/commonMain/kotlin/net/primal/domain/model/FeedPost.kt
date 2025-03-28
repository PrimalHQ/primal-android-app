package net.primal.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonArray
import net.primal.domain.EventLink
import net.primal.domain.EventUriNostrReference
import net.primal.domain.EventZap

data class FeedPost(
    val eventId: String,
    val author: FeedPostAuthor,
    val content: String,
    val tags: List<JsonArray>,
    val timestamp: Instant,
    val rawNostrEvent: String,
    val hashtags: List<String> = emptyList(),
    val replyToAuthor: FeedPostAuthor? = null,
    val reposts: List<FeedPostRepostInfo> = emptyList(),
    val stats: FeedPostStats? = null,
    val links: List<EventLink> = emptyList(),
    val nostrUris: List<EventUriNostrReference> = emptyList(),
    val eventZaps: List<EventZap> = emptyList(),
    val bookmark: PublicBookmark? = null,
)
