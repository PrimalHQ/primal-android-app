package net.primal.android.thread.ui

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feed.db.NoteZap

data class NoteZapUiModel(
    val avatarCdnImage: CdnImage? = null,
    val zappedAt: Long,
    val message: String,
    val amountInMillisats: String,
)

fun NoteZap.asNoteZapUiModel() =
    NoteZapUiModel(
        avatarCdnImage = this.zapSender?.avatarCdnImage,
        zappedAt = this.data.zappedAt,
        message = this.data.message,
        amountInMillisats = this.data.amountInMillisats,
    )
