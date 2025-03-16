package net.primal.android.nostr.ext

import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.articles.db.ArticleData
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.highlights.db.HighlightData
import net.primal.android.notes.db.PostData
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

fun List<NostrEvent>.mapAsPostDataPO(
    referencedPosts: List<PostData>,
    referencedArticles: List<ArticleData>,
    referencedHighlights: List<HighlightData>,
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }
    return map { it.shortTextNoteAsPost(referencedPostsMap, referencedArticlesMap, referencedHighlightsMap) }
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
    val replyToPostId = this.tags.find { it.hasReplyMarker() }?.getTagValueOrNull()
        ?: this.tags.find { it.hasRootMarker() }?.getTagValueOrNull()
        ?: this.tags.filterNot { it.hasMentionMarker() }.lastOrNull { it.isEventIdTag() }?.getTagValueOrNull()

    val replyToAuthorId = this.tags.find { it.hasReplyMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: this.tags.find { it.hasRootMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: referencedPostsMap[replyToPostId]?.authorId
        ?: referencedArticlesMap[replyToPostId]?.authorId
        ?: referencedHighlightsMap[replyToPostId]?.authorId

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        uris = this.content.parseUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = NostrJson.encodeToString(this.toJsonObject()),
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
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
        uris = content.parseUris(),
        hashtags = emptyList(),
        sig = this.sig,
        raw = NostrJson.encodeToString(this.toJsonObject()),
        replyToPostId = null,
        replyToAuthorId = null,
    )
}
