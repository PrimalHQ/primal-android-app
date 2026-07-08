package net.primal.android.notes.feed.note.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.kotest.matchers.shouldBe
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.model.NoteContentUi
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.EventUriType
import net.primal.domain.links.ReferencedUser
import org.junit.Test

class NoteContentRenderingTest {

    private val highlight = Color(0xFFFF0000)

    private fun render(
        data: NoteContentUi,
        expanded: Boolean = false,
        seeMoreText: String = "see more",
        shouldKeepNostrNoteUris: Boolean = false,
    ) = renderContentAsAnnotatedString(
        data = data,
        expanded = expanded,
        seeMoreText = seeMoreText,
        highlightColor = highlight,
        shouldKeepNostrNoteUris = shouldKeepNostrNoteUris,
    )

    private fun noteContent(
        content: String,
        uris: List<EventUriUi> = emptyList(),
        nostrUris: List<NoteNostrUriUi> = emptyList(),
        hashtags: List<String> = emptyList(),
        invoices: List<String> = emptyList(),
    ) = NoteContentUi(
        noteId = "n1",
        content = content,
        uris = uris,
        nostrUris = nostrUris,
        hashtags = hashtags,
        invoices = invoices,
    )

    private fun linkUri(url: String, title: String? = null) =
        EventUriUi(eventId = "e1", url = url, type = EventUriType.Other, title = title, position = 0)

    private fun mediaUri(url: String) =
        EventUriUi(eventId = "e1", url = url, type = EventUriType.Image, position = 0)

    private fun nostrUri(
        uri: String,
        type: EventUriNostrType = EventUriNostrType.Note,
        referencedUser: ReferencedUser? = null,
    ) = NoteNostrUriUi(
        uri = uri,
        type = type,
        referencedEventAlt = null,
        referencedHighlight = null,
        referencedNote = null,
        referencedArticle = null,
        referencedUser = referencedUser,
        referencedZap = null,
        referencedStream = null,
        position = 0,
    )

    private fun highlightSpan(start: Int, end: Int) =
        AnnotatedString.Range(SpanStyle(color = highlight), start, end)

    @Test
    fun `plain short text passes through without spans or annotations`() {
        val result = render(noteContent(content = "hello world"))

        result.text shouldBe "hello world"
        result.spanStyles shouldBe emptyList()
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `collapsed long text is ellipsized with a see-more span at the end`() {
        val result = render(noteContent(content = "a".repeat(400)))

        result.text shouldBe "a".repeat(300) + " see more"
        result.spanStyles shouldBe listOf(highlightSpan(start = 301, end = 309))
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `expanded long text is kept in full without spans`() {
        val result = render(noteContent(content = "a".repeat(400)), expanded = true)

        result.text shouldBe "a".repeat(400)
        result.spanStyles shouldBe emptyList()
    }

    @Test
    fun `empty seeMoreText on short content adds the zero-width end span quirk`() {
        val result = render(noteContent(content = "abc"), seeMoreText = "")

        result.text shouldBe "abc"
        result.spanStyles shouldBe listOf(highlightSpan(start = 3, end = 3))
    }

    @Test
    fun `untitled link stays in text and gets a url span and annotation`() {
        val url = "https://primal.example"
        val result = render(noteContent(content = "check $url out", uris = listOf(linkUri(url))))

        result.text shouldBe "check $url out"
        result.spanStyles shouldBe listOf(highlightSpan(start = 6, end = 6 + url.length))
        result.getStringAnnotations("url", 0, result.length) shouldBe
            listOf(AnnotatedString.Range(url, 6, 6 + url.length, "url"))
    }

    @Test
    fun `long url is ellipsized in text while the annotation carries the full url`() {
        val url = "https://example.com/" + "p".repeat(40)
        val shownUrl = url.take(40) + "..."
        val result = render(noteContent(content = "link: $url", uris = listOf(linkUri(url))))

        result.text shouldBe "link: $shownUrl"
        result.spanStyles shouldBe listOf(highlightSpan(start = 6, end = 6 + shownUrl.length))
        result.getStringAnnotations("url", 0, result.length) shouldBe
            listOf(AnnotatedString.Range(url, 6, 6 + shownUrl.length, "url"))
    }

    @Test
    fun `media url is removed from text without annotations`() {
        val url = "https://m.primal.net/img.jpg"
        val result = render(noteContent(content = "pic $url", uris = listOf(mediaUri(url))))

        result.text shouldBe "pic"
        result.spanStyles shouldBe emptyList()
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `titled link url is removed from text and produces no annotation`() {
        val url = "https://primal.example/article"
        val result = render(noteContent(content = "read $url", uris = listOf(linkUri(url, title = "Title"))))

        result.text shouldBe "read"
        result.spanStyles shouldBe emptyList()
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `profile mention is replaced with handle and annotated with the user id`() {
        val mention = nostrUri(
            uri = "nostr:npub1xyz",
            type = EventUriNostrType.Profile,
            referencedUser = ReferencedUser(userId = "uid-1", handle = "alice"),
        )
        val result = render(noteContent(content = "gm nostr:npub1xyz done", nostrUris = listOf(mention)))

        result.text shouldBe "gm @alice done"
        result.spanStyles shouldBe listOf(highlightSpan(start = 3, end = 9))
        result.getStringAnnotations("profileId", 0, result.length) shouldBe
            listOf(AnnotatedString.Range("uid-1", 3, 9, "profileId"))
    }

    @Test
    fun `note uri is removed from text`() {
        val result = render(
            noteContent(content = "see nostr:note1abc ok", nostrUris = listOf(nostrUri(uri = "nostr:note1abc"))),
        )

        result.text shouldBe "see  ok"
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `unhandled naddr uri is removed by default`() {
        val result = render(
            noteContent(content = "read nostr:naddr1qq now", nostrUris = listOf(nostrUri(uri = "nostr:naddr1qq"))),
        )

        result.text shouldBe "read  now"
        result.getStringAnnotations(0, result.length) shouldBe emptyList()
    }

    @Test
    fun `unhandled naddr uri is kept and annotated when nostr note uris are kept`() {
        val uri = "nostr:naddr1qq"
        val result = render(
            noteContent(content = "read $uri now", nostrUris = listOf(nostrUri(uri = uri))),
            shouldKeepNostrNoteUris = true,
        )

        result.text shouldBe "read $uri now"
        result.spanStyles shouldBe listOf(highlightSpan(start = 5, end = 5 + uri.length))
        result.getStringAnnotations("naddr", 0, result.length) shouldBe
            listOf(AnnotatedString.Range(uri, 5, 5 + uri.length, "naddr"))
    }

    @Test
    fun `hashtag gets a span and annotation`() {
        val result = render(noteContent(content = "hello #nostr world", hashtags = listOf("#nostr")))

        result.text shouldBe "hello #nostr world"
        result.spanStyles shouldBe listOf(highlightSpan(start = 6, end = 12))
        result.getStringAnnotations("hashtag", 0, result.length) shouldBe
            listOf(AnnotatedString.Range("#nostr", 6, 12, "hashtag"))
    }

    @Test
    fun `invoice text is removed`() {
        val result = render(noteContent(content = "pay lnbc123 pls", invoices = listOf("lnbc123")))

        result.text shouldBe "pay  pls"
    }

    @Test
    fun `line breaks are limited to two`() {
        val result = render(noteContent(content = "a\n\n\n\nb"))

        result.text shouldBe "a\n\nb"
    }

    @Test
    fun `url span comes before hashtag span and annotation order matches`() {
        val url = "https://a.co"
        val result = render(
            noteContent(content = "$url #x", uris = listOf(linkUri(url)), hashtags = listOf("#x")),
        )

        result.text shouldBe "$url #x"
        result.spanStyles shouldBe listOf(
            highlightSpan(start = 0, end = 12),
            highlightSpan(start = 13, end = 15),
        )
        result.getStringAnnotations("url", 0, result.length) shouldBe
            listOf(AnnotatedString.Range(url, 0, 12, "url"))
        result.getStringAnnotations("hashtag", 0, result.length) shouldBe
            listOf(AnnotatedString.Range("#x", 13, 15, "hashtag"))
    }
}
