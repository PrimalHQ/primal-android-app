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
import net.primal.domain.nostr.findFirstKindTag
import net.primal.domain.nostr.findReplyTargetId
import net.primal.domain.nostr.getPubkeyFromCommentEventTag
import net.primal.domain.nostr.getPubkeyFromReplyOrRootTag
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.hasReplyMarker
import net.primal.domain.nostr.hasRootMarker
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.isIMetaTag
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.parseHashtags
import net.primal.domain.nostr.utils.parseNostrUris

private val postDataKinds = setOf(
    NostrEventKind.ShortTextNote.value,
    NostrEventKind.Comment.value,
    NostrEventKind.Poll.value,
    NostrEventKind.ZapPoll.value,
)

fun List<NostrEvent>.mapAsPostDataPO(
    referencedPosts: List<PostData>,
    referencedArticles: List<ArticleData>,
    referencedHighlights: List<HighlightData>,
): List<PostData> {
    val referencedPostAuthorsById = referencedPosts.associate { it.postId to it.authorId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }
    return map { event ->
        event.nostrEventAsPost(
            referencedPostAuthorsById = referencedPostAuthorsById,
            referencedArticlesMap = referencedArticlesMap,
            referencedHighlightsMap = referencedHighlightsMap,
        )
    }
}

fun List<PrimalEvent>.mapNotNullAsPostDataPO(
    referencedArticles: List<ArticleData> = emptyList(),
    referencedHighlights: List<HighlightData> = emptyList(),
): List<PostData> {
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }

    val events = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }

    val postEvents = events.filter { event -> event.kind in postDataKinds }
    val pictureEvents = events.filter { event -> event.kind == NostrEventKind.PictureNote.value }
    val referencedPostAuthorsById = (postEvents + pictureEvents).associate { it.id to it.pubKey }

    val posts = postEvents.map {
        it.nostrEventAsPost(
            referencedPostAuthorsById = referencedPostAuthorsById,
            referencedArticlesMap = referencedArticlesMap,
            referencedHighlightsMap = referencedHighlightsMap,
        )
    }

    val pictures = pictureEvents.map { it.pictureNoteAsPost() }

    return posts + pictures
}

private data class ReplyReference(
    val replyToPostId: String?,
    val replyToAuthorId: String?,
)

private fun NostrEvent.resolveReplyReference(
    referencedPostAuthorsById: Map<String, String> = emptyMap(),
    referencedArticlesMap: Map<String, ArticleData> = emptyMap(),
    referencedHighlightsMap: Map<String, HighlightData> = emptyMap(),
): ReplyReference =
    if (this.kind == NostrEventKind.Comment.value) {
        resolveCommentReplyReference(referencedPostAuthorsById = referencedPostAuthorsById)
    } else {
        resolveNoteReplyReference(
            referencedPostAuthorsById = referencedPostAuthorsById,
            referencedArticlesMap = referencedArticlesMap,
            referencedHighlightsMap = referencedHighlightsMap,
        )
    }

private fun NostrEvent.resolveNoteReplyReference(
    referencedPostAuthorsById: Map<String, String>,
    referencedArticlesMap: Map<String, ArticleData>,
    referencedHighlightsMap: Map<String, HighlightData>,
): ReplyReference {
    val referencedNoteId = this.tags.findReplyTargetId()
        ?: return ReplyReference(replyToPostId = null, replyToAuthorId = null)

    val referencedNoteAuthorId = this.tags.find { it.hasReplyMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: this.tags.find { it.hasRootMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: referencedPostAuthorsById[referencedNoteId]
        ?: referencedArticlesMap[referencedNoteId]?.authorId
        ?: referencedHighlightsMap[referencedNoteId]?.authorId

    return ReplyReference(replyToPostId = referencedNoteId, replyToAuthorId = referencedNoteAuthorId)
}

private fun NostrEvent.resolveCommentReplyReference(referencedPostAuthorsById: Map<String, String>): ReplyReference {
    val parentKind = this.tags.findFirstKindTag()?.toIntOrNull()
    val parentEventTag = this.tags.lastOrNull { it.isEventIdTag() }
    val parentEventId = parentEventTag?.getTagValueOrNull()

    if (parentKind != NostrEventKind.Comment.value || parentEventId == null) {
        return ReplyReference(replyToPostId = null, replyToAuthorId = null)
    }

    val parentAuthorId = parentEventTag.getPubkeyFromCommentEventTag()?.takeIf { it.isNotEmpty() }
        ?: referencedPostAuthorsById[parentEventId]

    return ReplyReference(replyToPostId = parentEventId, replyToAuthorId = parentAuthorId)
}

private fun NostrEvent.nostrEventAsPost(
    referencedPostAuthorsById: Map<String, String> = emptyMap(),
    referencedArticlesMap: Map<String, ArticleData> = emptyMap(),
    referencedHighlightsMap: Map<String, HighlightData> = emptyMap(),
): PostData {
    val reply = resolveReplyReference(
        referencedPostAuthorsById = referencedPostAuthorsById,
        referencedArticlesMap = referencedArticlesMap,
        referencedHighlightsMap = referencedHighlightsMap,
    )

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        kind = this.kind,
        tags = this.tags,
        content = this.content,
        uris = this.content.detectUrls() + this.content.parseNostrUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        replyToPostId = reply.replyToPostId,
        replyToAuthorId = reply.replyToAuthorId,
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
        kind = this.kind,
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
