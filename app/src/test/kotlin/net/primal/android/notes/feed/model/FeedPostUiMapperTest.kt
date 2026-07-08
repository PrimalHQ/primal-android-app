package net.primal.android.notes.feed.model

import androidx.compose.ui.graphics.Color
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import net.primal.android.notes.feed.note.ui.renderContentAsAnnotatedString
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPostAuthor
import org.junit.Test

class FeedPostUiMapperTest {

    private fun feedPost(content: String, hashtags: List<String> = emptyList()) =
        FeedPost(
            eventId = "e1",
            author = FeedPostAuthor(authorId = "a1", handle = "alice", displayName = "Alice"),
            kind = 1,
            content = content,
            tags = emptyList(),
            timestamp = Instant.fromEpochSeconds(1_700_000_000),
            rawNostrEvent = "{}",
            hashtags = hashtags,
        )

    @Test
    fun `asFeedPostUi precomputes the collapsed note content`() {
        val postUi = feedPost(content = "hello #nostr " + "a".repeat(400), hashtags = listOf("#nostr")).asFeedPostUi()

        postUi.feedNoteContent shouldBe postUi.toNoteContentUi(
            content = computeFeedContent(content = postUi.content, uris = postUi.uris, nostrUris = postUi.nostrUris),
        )
    }

    @Test
    fun `precomputed rendering matches the lazy render pipeline`() {
        val postUi = feedPost(content = "hello #nostr " + "a".repeat(400), hashtags = listOf("#nostr")).asFeedPostUi()
        val rendered = postUi.feedNoteContentRendered.shouldNotBeNull()

        rendered.toAnnotatedString(seeMoreText = "see more", highlightColor = Color(0xFFFF0000)) shouldBe
            renderContentAsAnnotatedString(
                data = postUi.toNoteContentUi(
                    content = computeFeedContent(
                        content = postUi.content,
                        uris = postUi.uris,
                        nostrUris = postUi.nostrUris,
                    ),
                ),
                expanded = false,
                seeMoreText = "see more",
                highlightColor = Color(0xFFFF0000),
            )
    }

    @Test
    fun `short content precomputes without ellipsizing`() {
        val postUi = feedPost(content = "gm").asFeedPostUi()
        val rendered = postUi.feedNoteContentRendered.shouldNotBeNull()

        rendered.refinedText shouldBe "gm"
        rendered.shouldEllipsize shouldBe false
    }
}
