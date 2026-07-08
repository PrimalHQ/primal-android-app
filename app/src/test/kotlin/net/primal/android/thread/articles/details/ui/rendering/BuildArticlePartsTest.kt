package net.primal.android.thread.articles.details.ui.rendering

import io.kotest.matchers.shouldBe
import java.time.Instant
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.thread.articles.details.ArticleDetailsContract.ArticlePartRender
import net.primal.domain.nostr.cryptography.utils.hexToNoteHrp
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import org.junit.Test

class BuildArticlePartsTest {

    private val noteId = "5b3d615837b0f362dcb1c8068b098c7b4aa4fc8665f5696e33db7e1a572e3b0f"
    private val pubkey = "aa4fc8665f5696e33db7e1a572e3b0f5b3d615837b0f362dcb1c8068b098c7b4"

    private fun referencedNote(postId: String) = FeedPostUi(
        postId = postId,
        authorId = pubkey,
        authorName = "Alice",
        authorHandle = "alice",
        timestamp = Instant.EPOCH,
        content = "referenced note",
        stats = EventStatsUi(),
        rawNostrEventJson = "{}",
        kind = 1,
    )

    @Test
    fun `blank content produces no parts`() {
        buildArticleParts(
            content = "",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe emptyList()
    }

    @Test
    fun `plain markdown maps to single MarkdownRender`() {
        buildArticleParts(
            content = "Hello **world**",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(ArticlePartRender.MarkdownRender(markdown = "Hello **world**"))
    }

    @Test
    fun `markdown image splits into ImageRender`() {
        buildArticleParts(
            content = "before\n\n![](https://example.com/pic.png)\n\nafter",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(
            ArticlePartRender.MarkdownRender(markdown = "before\n\n"),
            ArticlePartRender.ImageRender(imageUrl = "https://example.com/pic.png"),
            ArticlePartRender.MarkdownRender(markdown = "\n\nafter"),
        )
    }

    @Test
    fun `linked markdown image keeps link url`() {
        buildArticleParts(
            content = "[![](https://example.com/pic.png)](https://example.com/target)",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(
            ArticlePartRender.ImageRender(
                imageUrl = "https://example.com/pic.png",
                linkUrl = "https://example.com/target",
            ),
        )
    }

    @Test
    fun `raw image url splits into ImageRender`() {
        buildArticleParts(
            content = "text https://example.com/photo.jpg more",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(
            ArticlePartRender.MarkdownRender(markdown = "text "),
            ArticlePartRender.ImageRender(imageUrl = "https://example.com/photo.jpg"),
            ArticlePartRender.MarkdownRender(markdown = " more"),
        )
    }

    @Test
    fun `markdown image with video extension maps to VideoRender`() {
        buildArticleParts(
            content = "![](https://example.com/clip.mp4)",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(ArticlePartRender.VideoRender(videoUrl = "https://example.com/clip.mp4"))
    }

    @Test
    fun `note uri with matching referenced note maps to NoteRender`() {
        val noteUri = "nostr:${noteId.hexToNoteHrp()}"
        val note = referencedNote(postId = noteId)

        buildArticleParts(
            content = "before\n\n$noteUri\n\nafter",
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = listOf(note),
        ) shouldBe listOf(
            ArticlePartRender.MarkdownRender(markdown = "before"),
            ArticlePartRender.NoteRender(note = note),
            ArticlePartRender.MarkdownRender(markdown = "after"),
        )
    }

    @Test
    fun `note uri without matching referenced note falls back to MarkdownRender`() {
        val noteUri = "nostr:${noteId.hexToNoteHrp()}"

        buildArticleParts(
            content = noteUri,
            npubToDisplayNameMap = emptyMap(),
            referencedNotes = emptyList(),
        ) shouldBe listOf(ArticlePartRender.MarkdownRender(markdown = noteUri))
    }

    @Test
    fun `npub uri is replaced with display name markdown link`() {
        val npub = pubkey.hexToNpubHrp()

        buildArticleParts(
            content = "GM nostr:$npub",
            npubToDisplayNameMap = mapOf(npub to "@Alice"),
            referencedNotes = emptyList(),
        ) shouldBe listOf(ArticlePartRender.MarkdownRender(markdown = "GM [@Alice](nostr:$npub)"))
    }
}
