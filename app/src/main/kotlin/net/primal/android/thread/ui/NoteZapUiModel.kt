package net.primal.android.thread.ui

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feed.db.NoteZap
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

data class NoteZapUiModel(
    val id: String,
    val avatarCdnImage: CdnImage? = null,
    val zappedAt: Long,
    val message: String?,
    val amountInSats: ULong,
)

fun NoteZap.asNoteZapUiModel() =
    NoteZapUiModel(
        id = "${this.data.zapSenderId};${this.data.noteId};${this.data.zapRequestAt}",
        avatarCdnImage = this.zapSender?.avatarCdnImage,
        zappedAt = this.data.zapRequestAt,
        message = this.data.message,
        amountInSats = this.data.amountInBtc.toBigDecimal().toSats(),
    )
