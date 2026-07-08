package net.primal.android.notes.feed.model

import io.kotest.matchers.shouldBe
import java.time.Instant
import net.primal.domain.nostr.NostrEventKind
import org.junit.Test

class FeedPostUiToNoteContentUiTest {

    private fun feedPostUi(content: String) =
        FeedPostUi(
            postId = "postId",
            authorId = "authorId",
            authorName = "authorName",
            authorHandle = "authorHandle",
            timestamp = Instant.EPOCH,
            content = content,
            stats = EventStatsUi(),
            rawNostrEventJson = "",
            kind = NostrEventKind.ShortTextNote.value,
        )

    @Test
    fun `toNoteContentUi uses the provided content override`() {
        val post = feedPostUi(content = "FULL CONTENT")

        post.toNoteContentUi(content = "TRIMMED").content shouldBe "TRIMMED"
    }

    @Test
    fun `toNoteContentUi defaults to the full content`() {
        val post = feedPostUi(content = "FULL CONTENT")

        post.toNoteContentUi().content shouldBe "FULL CONTENT"
    }
}
