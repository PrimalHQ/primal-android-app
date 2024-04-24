package net.primal.android.thread.ui

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feed.db.NoteZap

data class NoteZapUiModel(
    val id: String,
    val avatarCdnImage: CdnImage? = null,
    val zappedAt: Long,
    val message: String,
    val amountInMillisats: String,
)

fun NoteZap.asNoteZapUiModel() =
    NoteZapUiModel(
        id = "${this.data.zapSenderId};${this.data.noteId};${this.data.zappedAt}",
        avatarCdnImage = this.zapSender?.avatarCdnImage,
        zappedAt = this.data.zappedAt,
        message = this.data.message,
        amountInMillisats = this.data.amountInMillisats,
    )
