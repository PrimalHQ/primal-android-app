package net.primal.android.notes.feed.model

import fr.acinq.lightning.payment.Bolt11Invoice
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.android.notes.db.PostData
import net.primal.android.wallet.utils.LnInvoiceUtils

data class NoteContentUi(
    val noteId: String,
    val content: String,
    val attachments: List<NoteAttachmentUi> = emptyList(),
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
        attachments = this.attachments.sortedBy { it.position },
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
        attachments = this.attachments.sortedBy { it.position },
        nostrUris = this.nostrUris.sortedBy { it.position },
        hashtags = this.hashtags,
        invoices = invoices,
        blossoms = emptyList(),
    )
}

fun PostData.toNoteContentUi(nostrUris: List<NoteNostrUriUi> = emptyList()): NoteContentUi {
    return NoteContentUi(
        noteId = this.postId,
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
