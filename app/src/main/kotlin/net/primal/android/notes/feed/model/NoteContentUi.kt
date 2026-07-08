package net.primal.android.notes.feed.model

import androidx.compose.runtime.Immutable
import fr.acinq.lightning.payment.Bolt11Invoice
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.domain.common.util.isPrimalIdentifier
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedStream
import net.primal.domain.links.ReferencedZap
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.posts.FeedPost

@Immutable
data class NoteContentUi(
    val noteId: String,
    val content: String,
    val uris: List<EventUriUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val invoices: List<String> = emptyList(),
    val blossoms: List<String> = emptyList(),
    val poll: PollUi? = null,
) {
    val partitions: NoteContentPartitions = computePartitions(uris = uris, nostrUris = nostrUris)
}

@Immutable
data class NoteContentPartitions(
    val referencedStreams: List<ReferencedStream>,
    val referencedHighlights: List<ReferencedHighlight>,
    val referencedNotes: List<NoteNostrUriUi>,
    val referencedArticles: List<NoteNostrUriUi>,
    val referencedZaps: List<ReferencedZap>,
    val unsupportedEvents: List<NoteNostrUriUi>,
    val filteredEventUris: List<EventUriUi>,
)

private fun computePartitions(uris: List<EventUriUi>, nostrUris: List<NoteNostrUriUi>): NoteContentPartitions {
    val existingNostrUris = nostrUris
        .filter { it.type == EventUriNostrType.Note || it.type == EventUriNostrType.Article }
        .map { it.uri }
        .toSet()

    return NoteContentPartitions(
        referencedStreams = nostrUris
            .filter { it.type == EventUriNostrType.Stream }
            .mapNotNull { it.referencedStream },
        referencedHighlights = nostrUris
            .filter { it.type == EventUriNostrType.Highlight }
            .mapNotNull { it.referencedHighlight },
        referencedNotes = nostrUris.filter { it.type == EventUriNostrType.Note },
        referencedArticles = nostrUris.filter { it.type == EventUriNostrType.Article },
        referencedZaps = nostrUris
            .filter { it.type == EventUriNostrType.Zap }
            .mapNotNull { it.referencedZap },
        unsupportedEvents = nostrUris.filter { it.type == EventUriNostrType.Unsupported },
        filteredEventUris = uris.filterNot { uriItem ->
            uriItem.url.isPrimalIdentifier() && uriItem.url.substringAfterLast("/") in existingNostrUris
        },
    )
}

fun FeedPostUi.toNoteContentUi(content: String = this.content): NoteContentUi {
    val invoices = mutableListOf<String>()
    content.extractValidInvoiceOrNull()?.let { lnbc -> invoices.add(lnbc) }
    return NoteContentUi(
        noteId = this.postId,
        content = content,
        uris = this.uris.sortedBy { it.position },
        nostrUris = this.nostrUris.sortedBy { it.position },
        hashtags = this.hashtags,
        invoices = invoices,
        blossoms = this.authorBlossoms,
        poll = this.poll,
    )
}

fun ChatMessageUi.toNoteContentUi(): NoteContentUi {
    val invoices = mutableListOf<String>()
    this.content.extractValidInvoiceOrNull()?.let { lnbc -> invoices.add(lnbc) }
    return NoteContentUi(
        noteId = this.messageId,
        content = this.content,
        uris = this.uris.sortedBy { it.position },
        nostrUris = this.nostrUris.sortedBy { it.position },
        hashtags = this.hashtags,
        invoices = invoices,
        blossoms = emptyList(),
    )
}

fun FeedPost.toNoteContentUi(nostrUris: List<NoteNostrUriUi> = emptyList()): NoteContentUi {
    return NoteContentUi(
        noteId = this.eventId,
        content = this.content,
        nostrUris = nostrUris.sortedBy { it.position },
        hashtags = this.hashtags,
        invoices = emptyList(),
        blossoms = emptyList(),
    )
}

private fun String.extractValidInvoiceOrNull(): String? {
    return LnInvoiceUtils.findInvoice(this)?.let { lnbc ->
        if (Bolt11Invoice.read(lnbc).isSuccess) lnbc else null
    }
}
