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
import net.primal.domain.nostr.findReplyTargetId
import net.primal.domain.nostr.getPubkeyFromReplyOrRootTag
import net.primal.domain.nostr.hasReplyMarker
import net.primal.domain.nostr.hasRootMarker
import net.primal.domain.nostr.isIMetaTag
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
    return map { event ->
        event.nostrEventAsPost(
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

    val postKinds = setOf(
        NostrEventKind.ShortTextNote.value,
        NostrEventKind.Poll.value,
        NostrEventKind.ZapPoll.value,
    )

    val events = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }

    val posts = events
        .filter { event -> event.kind in postKinds }
        .map {
            it.nostrEventAsPost(
                referencedPostsMap = referencedPostsMap,
                referencedArticlesMap = referencedArticlesMap,
                referencedHighlightsMap = referencedHighlightsMap,
            )
        }

    val pictures = events
        .filter { event -> event.kind == NostrEventKind.PictureNote.value }
        .map { it.pictureNoteAsPost() }

    return posts + pictures
}

private data class ReplyReference(
    val replyToPostId: String?,
    val replyToAuthorId: String?,
)

private fun NostrEvent.resolveReplyReference(
    referencedPostsMap: Map<String, PostData> = emptyMap(),
    referencedArticlesMap: Map<String, ArticleData> = emptyMap(),
    referencedHighlightsMap: Map<String, HighlightData> = emptyMap(),
): ReplyReference {
    val referencedNoteId = this.tags.findReplyTargetId()
        ?: return ReplyReference(replyToPostId = null, replyToAuthorId = null)

    val referencedNoteAuthorId = this.tags.find { it.hasReplyMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: this.tags.find { it.hasRootMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: referencedPostsMap[referencedNoteId]?.authorId
        ?: referencedArticlesMap[referencedNoteId]?.authorId
        ?: referencedHighlightsMap[referencedNoteId]?.authorId

    return ReplyReference(replyToPostId = referencedNoteId, replyToAuthorId = referencedNoteAuthorId)
}

private fun NostrEvent.nostrEventAsPost(
    referencedPostsMap: Map<String, PostData> = emptyMap(),
    referencedArticlesMap: Map<String, ArticleData> = emptyMap(),
    referencedHighlightsMap: Map<String, HighlightData> = emptyMap(),
): PostData {
    val reply = resolveReplyReference(
        referencedPostsMap = referencedPostsMap,
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
