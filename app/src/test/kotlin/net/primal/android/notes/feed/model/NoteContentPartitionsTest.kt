package net.primal.android.notes.feed.model

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.EventUriType
import net.primal.domain.links.ReferencedHighlight
import org.junit.Test

class NoteContentPartitionsTest {

    private fun nostrUri(
        uri: String,
        type: EventUriNostrType,
        referencedHighlight: ReferencedHighlight? = null,
    ) = NoteNostrUriUi(
        uri = uri,
        type = type,
        referencedEventAlt = null,
        referencedHighlight = referencedHighlight,
        referencedNote = null,
        referencedArticle = null,
        referencedUser = null,
        referencedZap = null,
        referencedStream = null,
        position = 0,
    )

    private fun eventUri(url: String) = EventUriUi(eventId = "e1", url = url, type = EventUriType.Other, position = 0)

    private fun noteContent(
        uris: List<EventUriUi> = emptyList(),
        nostrUris: List<NoteNostrUriUi> = emptyList(),
    ) = NoteContentUi(noteId = "n1", content = "irrelevant", uris = uris, nostrUris = nostrUris)

    @Test
    fun `nostr uris are partitioned by type`() {
        val note = nostrUri(uri = "nostr:note1a", type = EventUriNostrType.Note)
        val article = nostrUri(uri = "nostr:naddr1b", type = EventUriNostrType.Article)
        val unsupported = nostrUri(uri = "nostr:nevent1c", type = EventUriNostrType.Unsupported)
        val partitions = noteContent(nostrUris = listOf(note, article, unsupported)).partitions

        partitions.referencedNotes shouldBe listOf(note)
        partitions.referencedArticles shouldBe listOf(article)
        partitions.unsupportedEvents shouldBe listOf(unsupported)
    }

    @Test
    fun `referenced payloads are extracted and missing payloads are dropped`() {
        val highlight = ReferencedHighlight(text = "t", eventId = "e", authorId = "a", aTag = buildJsonArray {})
        val withPayload = nostrUri(
            uri = "nostr:nevent1h",
            type = EventUriNostrType.Highlight,
            referencedHighlight = highlight,
        )
        val withoutPayload = nostrUri(uri = "nostr:nevent1x", type = EventUriNostrType.Highlight)
        val partitions = noteContent(nostrUris = listOf(withPayload, withoutPayload)).partitions

        partitions.referencedHighlights shouldBe listOf(highlight)
        partitions.referencedStreams shouldBe emptyList()
        partitions.referencedZaps shouldBe emptyList()
    }

    @Test
    fun `event uris duplicating referenced notes via primal links are filtered out`() {
        val note = nostrUri(uri = "note1abc", type = EventUriNostrType.Note)
        val duplicate = eventUri(url = "https://primal.net/e/note1abc")
        val kept = eventUri(url = "https://example.com/e/note1abc")
        val partitions = noteContent(uris = listOf(duplicate, kept), nostrUris = listOf(note)).partitions

        partitions.filteredEventUris shouldBe listOf(kept)
    }
}
