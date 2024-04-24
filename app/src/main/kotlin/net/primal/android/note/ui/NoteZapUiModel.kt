package net.primal.android.note.ui

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.note.db.NoteZap
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

data class NoteZapUiModel(
    val id: String,
    val zapperId: String,
    val zapperName: String,
    val zapperHandle: String,
    val zapperInternetIdentifier: String? = null,
    val zapperAvatarCdnImage: CdnImage? = null,
    val zappedAt: Long,
    val message: String?,
    val amountInSats: ULong,
)

fun NoteZap.asNoteZapUiModel() =
    NoteZapUiModel(
        id = "${this.data.zapSenderId};${this.data.noteId};${this.data.zapRequestAt}",
        zapperAvatarCdnImage = this.zapSender?.avatarCdnImage,
        zapperId = this.zapSender?.ownerId ?: this.data.zapSenderId,
        zapperName = this.zapSender?.authorNameUiFriendly() ?: this.data.zapSenderId.asEllipsizedNpub(),
        zapperHandle = this.zapSender?.usernameUiFriendly() ?: this.data.zapSenderId.asEllipsizedNpub(),
        zapperInternetIdentifier = this.zapSender?.internetIdentifier?.formatNip05Identifier(),
        zappedAt = this.data.zapRequestAt,
        message = this.data.message,
        amountInSats = this.data.amountInBtc.toBigDecimal().toSats(),
    )
