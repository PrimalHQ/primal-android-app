package net.primal.repository.processors.mappers

import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.utils.parseHashtags
import net.primal.db.notes.PostData
import net.primal.networking.model.NostrEvent
import net.primal.networking.model.NostrEventKind
import net.primal.networking.model.primal.PrimalEvent
import net.primal.repository.getPubkeyFromReplyOrRootTag
import net.primal.repository.getTagValueOrNull
import net.primal.repository.hasMentionMarker
import net.primal.repository.hasReplyMarker
import net.primal.repository.hasRootMarker
import net.primal.repository.isEventIdTag
import net.primal.repository.isIMetaTag
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.toJsonObject

// TODO Rewire articles, highlights and parsing uris once ported

fun List<NostrEvent>.mapAsPostDataPO(
    referencedPosts: List<PostData>,
//    referencedArticles: List<ArticleData>,
//    referencedHighlights: List<HighlightData>,
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
//    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
//    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }
    return map {
        it.shortTextNoteAsPost(
            referencedPostsMap = referencedPostsMap,
//            referencedArticlesMap = referencedArticlesMap,
//            referencedHighlightsMap = referencedHighlightsMap,
        )
    }
}

fun List<PrimalEvent>.mapNotNullAsPostDataPO(
    referencedPosts: List<PostData> = emptyList(),
//    referencedArticles: List<ArticleData> = emptyList(),
//    referencedHighlights: List<HighlightData> = emptyList(),
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
//    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
//    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }

    val events = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
    val notes = events
        .filter { event -> event.kind == NostrEventKind.ShortTextNote.value }
        .map {
            it.shortTextNoteAsPost(
                referencedPostsMap = referencedPostsMap,
//                referencedArticlesMap = referencedArticlesMap,
//                referencedHighlightsMap = referencedHighlightsMap,
            )
        }

    val pictures = events
        .filter { event -> event.kind == NostrEventKind.PictureNote.value }
        .map { it.pictureNoteAsPost() }

    return notes + pictures
}

private fun NostrEvent.shortTextNoteAsPost(
    referencedPostsMap: Map<String, PostData>,
//    referencedArticlesMap: Map<String, ArticleData>,
//    referencedHighlightsMap: Map<String, HighlightData>,
): PostData {
    val replyToPostId = this.tags.find { it.hasReplyMarker() }?.getTagValueOrNull()
        ?: this.tags.find { it.hasRootMarker() }?.getTagValueOrNull()
        ?: this.tags.filterNot { it.hasMentionMarker() }.lastOrNull { it.isEventIdTag() }?.getTagValueOrNull()

    val replyToAuthorId = this.tags.find { it.hasReplyMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: this.tags.find { it.hasRootMarker() }?.getPubkeyFromReplyOrRootTag()
        ?: referencedPostsMap[replyToPostId]?.authorId
//        ?: referencedArticlesMap[replyToPostId]?.authorId
//        ?: referencedHighlightsMap[replyToPostId]?.authorId

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        // TODO Rewire with parseUris() when implemented
//        uris = this.content.parseUris(),
        uris = emptyList(),
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
        // TODO Rewire with parseUris() when implemented
//        uris = content.parseUris(),
        uris = emptyList(),
        hashtags = emptyList(),
        sig = this.sig,
        raw = NostrJson.encodeToString(this.toJsonObject()),
        replyToPostId = null,
        replyToAuthorId = null,
    )
}
