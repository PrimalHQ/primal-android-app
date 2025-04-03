package net.primal.android.notes.feed.model

import fr.acinq.lightning.payment.Bolt11Invoice
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.domain.model.FeedPost
import net.primal.domain.nostr.utils.LnInvoiceUtils

data class NoteContentUi(
    val noteId: String,
    val content: String,
    val uris: List<EventUriUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val invoices: List<String> = emptyList(),
    val blossoms: List<String> = emptyList(),
)

fun FeedPostUi.toNoteContentUi(): NoteContentUi {
    val invoices = mutableListOf<String>()
    this.content.extractValidInvoiceOrNull()?.let { lnbc -> invoices.add(lnbc) }
    return NoteContentUi(
        noteId = this.postId,
        content = this.content,
        uris = this.uris.sortedBy { it.position },
        nostrUris = this.nostrUris.sortedBy { it.position },
        hashtags = this.hashtags,
        invoices = invoices,
        blossoms = this.authorBlossoms,
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
