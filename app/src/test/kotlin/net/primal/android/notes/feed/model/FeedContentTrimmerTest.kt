package net.primal.android.notes.feed.model

import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.note.ui.renderContentAsAnnotatedString
import net.primal.domain.links.EventUriType
import org.junit.Test

class FeedContentTrimmerTest {

    private fun mediaUri(url: String) =
        EventUriUi(eventId = "eventId", url = url, type = EventUriType.Image, position = 0)

    private fun renderFeed(data: NoteContentUi) =
        renderContentAsAnnotatedString(
            data = data,
            expanded = false,
            seeMoreText = "see more",
            highlightColor = Color(0xFFFF0000),
        )

    @Test
    fun `returns content unchanged when shorter than the trim budget`() {
        val content = "a".repeat(100)

        val result = computeFeedContent(content = content, uris = emptyList(), nostrUris = emptyList())

        result shouldBe content
    }

    @Test
    fun `trims a long plain-text note to just past the threshold`() {
        val content = "a".repeat(1000)

        val result = computeFeedContent(content = content, uris = emptyList(), nostrUris = emptyList())

        result.length shouldBe FEED_CONTENT_TRIM_BUDGET + 1
        content.startsWith(result) shouldBe true
    }

    @Test
    fun `keeps full content when it is almost entirely a media url`() {
        val url = "https://image.example.com/" + "x".repeat(500)

        val result = computeFeedContent(content = url, uris = listOf(mediaUri(url)), nostrUris = emptyList())

        result shouldBe url
    }

    @Test
    fun `excludes media urls from the budget so url-heavy prefixes are kept`() {
        val urls = (0 until 20).map { "https://image.example.com/img$it.jpg" }
        val urlBlock = urls.joinToString(separator = "")
        val text = "b".repeat(1000)
        val content = urlBlock + text

        val result = computeFeedContent(
            content = content,
            uris = urls.map { mediaUri(it) },
            nostrUris = emptyList(),
        )

        result.length shouldBe urlBlock.length + FEED_CONTENT_TRIM_BUDGET + 1
        result.startsWith(urlBlock) shouldBe true
    }

    @Test
    fun `trimmed feed content renders identically to full content for a long plain-text note`() {
        val content = "lorem ipsum dolor sit amet ".repeat(60)
        val feedContent = computeFeedContent(content = content, uris = emptyList(), nostrUris = emptyList())

        val full = NoteContentUi(noteId = "noteId", content = content)
        val trimmed = full.copy(content = feedContent)

        renderFeed(trimmed) shouldBe renderFeed(full)
    }

    @Test
    fun `trimmed feed content renders identically to full content when a media url precedes long text`() {
        val url = "https://image.example.com/photo.jpg"
        val content = url + "\n" + "some text here ".repeat(60)
        val uris = listOf(mediaUri(url))
        val feedContent = computeFeedContent(content = content, uris = uris, nostrUris = emptyList())

        val full = NoteContentUi(noteId = "noteId", content = content, uris = uris)
        val trimmed = full.copy(content = feedContent)

        renderFeed(trimmed) shouldBe renderFeed(full)
    }
}
