package net.primal.android.notes.feed.model

import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.Test

class FeedPostUiToNoteContentUiTest {

    private fun feedPostUi(content: String, feedContent: String) =
        FeedPostUi(
            postId = "postId",
            authorId = "authorId",
            authorName = "authorName",
            authorHandle = "authorHandle",
            timestamp = Instant.EPOCH,
            content = content,
            feedContent = feedContent,
            stats = EventStatsUi(),
            rawNostrEventJson = "",
        )

    @Test
    fun `toNoteContentUi uses the trimmed feedContent when not showing full content`() {
        val post = feedPostUi(content = "FULL CONTENT", feedContent = "TRIMMED")

        post.toNoteContentUi(useFullContent = false).content shouldBe "TRIMMED"
    }

    @Test
    fun `toNoteContentUi uses the full content when showing full content`() {
        val post = feedPostUi(content = "FULL CONTENT", feedContent = "TRIMMED")

        post.toNoteContentUi(useFullContent = true).content shouldBe "FULL CONTENT"
    }

    @Test
    fun `toNoteContentUi defaults to full content`() {
        val post = feedPostUi(content = "FULL CONTENT", feedContent = "TRIMMED")

        post.toNoteContentUi().content shouldBe "FULL CONTENT"
    }
}
