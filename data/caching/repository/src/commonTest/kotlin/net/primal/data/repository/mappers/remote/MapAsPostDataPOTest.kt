package net.primal.data.repository.mappers.remote

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import net.primal.data.local.dao.notes.PostData
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class MapAsPostDataPOTest {

    private val authorId = "comment_author_pubkey"
    private val parentCommentId = "parent_comment_event_id"
    private val parentCommentAuthorId = "parent_comment_author_pubkey"
    private val articleEventId = "article_event_id"
    private val articleAuthorId = "article_author_pubkey"
    private val articleCoordinate = "30023:$articleAuthorId:article-identifier"
    private val quotedEventId = "quoted_event_id"

    private fun tag(vararg values: String): JsonArray = buildJsonArray { values.forEach { add(JsonPrimitive(it)) } }

    private fun buildNostrEvent(kind: Int, tags: List<JsonArray>): NostrEvent {
        return NostrEvent(
            id = "event_id",
            pubKey = authorId,
            createdAt = 1000L,
            kind = kind,
            tags = tags,
            content = "some content",
            sig = "signature",
        )
    }

    private fun NostrEvent.mapSingle(referencedPosts: List<PostData> = emptyList()): PostData =
        listOf(this).mapAsPostDataPO(
            referencedPosts = referencedPosts,
            referencedArticles = emptyList(),
            referencedHighlights = emptyList(),
        ).first()

    private fun buildCommentReplyTags(parentEventTag: JsonArray) =
        listOf(
            tag("A", articleCoordinate),
            tag("K", "30023"),
            tag("P", articleAuthorId),
            parentEventTag,
            tag("k", NostrEventKind.Comment.value.toString()),
            tag("p", parentCommentAuthorId),
        )

    @Test
    fun mapAsPostDataPO_resolvesCommentReplyParent_fromCommentEventTag() {
        val event = buildNostrEvent(
            kind = NostrEventKind.Comment.value,
            tags = buildCommentReplyTags(
                parentEventTag = tag("e", parentCommentId, "wss://relay.primal.net", parentCommentAuthorId),
            ),
        )

        val actual = event.mapSingle()

        actual.replyToPostId shouldBe parentCommentId
        actual.replyToAuthorId shouldBe parentCommentAuthorId
    }

    @Test
    fun mapAsPostDataPO_resolvesCommentReplyParent_evenWhenQuoteTagPresent() {
        val event = buildNostrEvent(
            kind = NostrEventKind.Comment.value,
            tags = buildCommentReplyTags(
                parentEventTag = tag("e", parentCommentId, "wss://relay.primal.net", parentCommentAuthorId),
            ) + listOf(tag("q", quotedEventId, "wss://relay.primal.net", "quoted_author_pubkey")),
        )

        val actual = event.mapSingle()

        actual.replyToPostId shouldBe parentCommentId
        actual.replyToAuthorId shouldBe parentCommentAuthorId
    }

    @Test
    fun mapAsPostDataPO_resolvesCommentReplyAuthor_fromReferencedPosts_whenPubkeyMissingInTag() {
        val event = buildNostrEvent(
            kind = NostrEventKind.Comment.value,
            tags = buildCommentReplyTags(
                parentEventTag = tag("e", parentCommentId),
            ),
        )
        val referencedParent = PostData(
            postId = parentCommentId,
            authorId = parentCommentAuthorId,
            createdAt = 500L,
            kind = NostrEventKind.Comment.value,
            tags = emptyList(),
            content = "parent content",
            uris = emptyList(),
            hashtags = emptyList(),
            sig = "parent signature",
            raw = "{}",
        )

        val actual = event.mapSingle(referencedPosts = listOf(referencedParent))

        actual.replyToPostId shouldBe parentCommentId
        actual.replyToAuthorId shouldBe parentCommentAuthorId
    }

    @Test
    fun mapAsPostDataPO_leavesReplyReferenceEmpty_forTopLevelArticleComment() {
        val event = buildNostrEvent(
            kind = NostrEventKind.Comment.value,
            tags = listOf(
                tag("A", articleCoordinate),
                tag("K", "30023"),
                tag("P", articleAuthorId),
                tag("a", articleCoordinate),
                tag("e", articleEventId, "wss://relay.primal.net", articleAuthorId),
                tag("k", "30023"),
                tag("p", articleAuthorId),
            ),
        )

        val actual = event.mapSingle()

        actual.replyToPostId shouldBe null
        actual.replyToAuthorId shouldBe null
    }

    @Test
    fun mapAsPostDataPO_keepsCommentKind() {
        val event = buildNostrEvent(
            kind = NostrEventKind.Comment.value,
            tags = buildCommentReplyTags(
                parentEventTag = tag("e", parentCommentId, "wss://relay.primal.net", parentCommentAuthorId),
            ),
        )

        val actual = event.mapSingle()

        actual.kind shouldBe NostrEventKind.Comment.value
    }

    @Test
    fun mapAsPostDataPO_resolvesShortTextNoteReply_fromNip10Markers() {
        val event = buildNostrEvent(
            kind = NostrEventKind.ShortTextNote.value,
            tags = listOf(
                tag("e", "root_event_id", "wss://relay.primal.net", "root", "root_author_pubkey"),
                tag("e", parentCommentId, "wss://relay.primal.net", "reply", parentCommentAuthorId),
            ),
        )

        val actual = event.mapSingle()

        actual.replyToPostId shouldBe parentCommentId
        actual.replyToAuthorId shouldBe parentCommentAuthorId
    }

    @Test
    fun mapAsPostDataPO_leavesReplyReferenceEmpty_forShortTextNoteWithQuoteTag() {
        val event = buildNostrEvent(
            kind = NostrEventKind.ShortTextNote.value,
            tags = listOf(
                tag("e", quotedEventId, "wss://relay.primal.net"),
                tag("q", quotedEventId, "wss://relay.primal.net"),
            ),
        )

        val actual = event.mapSingle()

        actual.replyToPostId shouldBe null
        actual.replyToAuthorId shouldBe null
    }
}
