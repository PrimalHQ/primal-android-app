package net.primal.data.repository.mappers.remote

import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.utils.detectUrls
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.local.dao.reads.HighlightData
import net.primal.domain.common.PrimalEvent
import net.primal.domain.common.util.takeContentOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.getPubkeyFromReplyOrRootTag
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.hasMentionMarker
import net.primal.domain.nostr.hasReplyMarker
import net.primal.domain.nostr.hasRootMarker
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.isIMetaTag
import net.primal.domain.nostr.isQuoteTag
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.parseHashtags
import net.primal.domain.nostr.utils.parseNostrUris

fun List<NostrEvent>.mapAsPostDataPO(
    referencedPosts: List<PostData>,
    referencedArticles: List<ArticleData>,
    referencedHighlights: List<HighlightData>,
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }
    return map {
        it.shortTextNoteAsPost(
            referencedPostsMap = referencedPostsMap,
            referencedArticlesMap = referencedArticlesMap,
            referencedHighlightsMap = referencedHighlightsMap,
        )
    }
}

fun List<PrimalEvent>.mapNotNullAsPostDataPO(
    referencedPosts: List<PostData> = emptyList(),
    referencedArticles: List<ArticleData> = emptyList(),
    referencedHighlights: List<HighlightData> = emptyList(),
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }

    val events = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
    val notes = events
        .filter { event -> event.kind == NostrEventKind.ShortTextNote.value }
        .map {
            it.shortTextNoteAsPost(
                referencedPostsMap = referencedPostsMap,
                referencedArticlesMap = referencedArticlesMap,
                referencedHighlightsMap = referencedHighlightsMap,
            )
        }

    val pictures = events
        .filter { event -> event.kind == NostrEventKind.PictureNote.value }
        .map { it.pictureNoteAsPost() }

    return notes + pictures
}

private fun NostrEvent.shortTextNoteAsPost(
    referencedPostsMap: Map<String, PostData>,
    referencedArticlesMap: Map<String, ArticleData>,
    referencedHighlightsMap: Map<String, HighlightData>,
): PostData {
    val isQuote = this.tags.any { it.isQuoteTag() }
    val isReply = !isQuote && (
        this.tags.any { it.hasReplyMarker() } || this.tags.any { it.hasRootMarker() } ||
            this.tags.any { it.isEventIdTag() && !it.hasMentionMarker() }
        )

    val referencedNoteId = this.tags.find { it.hasReplyMarker() }?.getTagValueOrNull()
        ?: this.tags.find { it.hasRootMarker() }?.getTagValueOrNull()
        ?: this.tags.filterNot { it.hasMentionMarker() }.lastOrNull { it.isEventIdTag() }?.getTagValueOrNull()

    val referencedNoteAuthorId = this.tags.find { it.hasReplyMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: this.tags.find { it.hasRootMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: referencedPostsMap[referencedNoteId]?.authorId
        ?: referencedArticlesMap[referencedNoteId]?.authorId
        ?: referencedHighlightsMap[referencedNoteId]?.authorId

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        uris = this.content.detectUrls() + this.content.parseNostrUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        replyToPostId = if (isReply) referencedNoteId else null,
        replyToAuthorId = if (isReply) referencedNoteAuthorId else null,
    )
}

private fun NostrEvent.pictureNoteAsPost(): PostData {
    val iMetaTags = this.tags.filter { it.isIMetaTag() }
    val imageUrls = iMetaTags.mapNotNull { it.getOrNull(1)?.jsonPrimitive?.content?.split(" ")?.lastOrNull() }
    val content = imageUrls.joinToString("\n")
    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = content,
        uris = content.detectUrls() + content.parseNostrUris(),
        hashtags = emptyList(),
        sig = this.sig,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        replyToPostId = null,
        replyToAuthorId = null,
    )
}
