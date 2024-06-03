package net.primal.android.core.compose.feed.model

import fr.acinq.lightning.payment.Bolt11Invoice
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.android.wallet.utils.LnInvoiceUtils

data class NoteContentUi(
    val noteId: String,
    val content: String,
    val attachments: List<NoteAttachmentUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val invoices: List<String> = emptyList(),
)

fun FeedPostUi.toNoteContentUi(): NoteContentUi {
    val invoices = mutableListOf<String>()
    this.content.extractValidInvoiceOrNull()?.let { lnbc -> invoices.add(lnbc) }
    return NoteContentUi(
        noteId = this.postId,
        content = this.content,
        attachments = this.attachments,
        nostrUris = this.nostrUris,
        hashtags = this.hashtags,
        invoices = invoices,
    )
}

fun ChatMessageUi.toNoteContentUi(): NoteContentUi {
    val invoices = mutableListOf<String>()
    this.content.extractValidInvoiceOrNull()?.let { lnbc -> invoices.add(lnbc) }
    return NoteContentUi(
        noteId = this.messageId,
        content = this.content,
        attachments = this.attachments,
        nostrUris = this.nostrUris,
        hashtags = this.hashtags,
        invoices = invoices,
    )
}

private fun String.extractValidInvoiceOrNull(): String? {
    return LnInvoiceUtils.findInvoice(this)?.let { lnbc ->
        if (Bolt11Invoice.read(lnbc).isSuccess) lnbc else null
    }
}
