package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.articles.db.ArticleData
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.highlights.db.HighlightData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.notes.db.PostData

fun List<NostrEvent>.mapAsPostDataPO(
    referencedPosts: List<PostData>,
    referencedArticles: List<ArticleData>,
    referencedHighlights: List<HighlightData>,
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }
    return map { it.asPost(referencedPostsMap, referencedArticlesMap, referencedHighlightsMap) }
}

fun List<PrimalEvent>.mapNotNullAsPostDataPO(
    referencedPosts: List<PostData> = emptyList(),
    referencedArticles: List<ArticleData> = emptyList(),
    referencedHighlights: List<HighlightData> = emptyList(),
): List<PostData> {
    val referencedPostsMap = referencedPosts.associateBy { it.postId }
    val referencedArticlesMap = referencedArticles.associateBy { it.articleId }
    val referencedHighlightsMap = referencedHighlights.associateBy { it.highlightId }

    return this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
        .filter { event -> event.kind == NostrEventKind.ShortTextNote.value }
        .map {
            it.asPost(
                referencedPostsMap = referencedPostsMap,
                referencedArticlesMap = referencedArticlesMap,
                referencedHighlightsMap = referencedHighlightsMap,
            )
        }
}

fun NostrEvent.asPost(
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
