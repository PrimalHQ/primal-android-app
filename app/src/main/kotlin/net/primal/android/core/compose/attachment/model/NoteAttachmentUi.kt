package net.primal.android.core.compose.attachment.model

import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.attachments.domain.NoteAttachmentType

data class NoteAttachmentUi(
    val url: String,
    val type: NoteAttachmentType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val authorAvatarUrl: String? = null,
)

fun NoteAttachment.asNoteAttachmentUi() =
    NoteAttachmentUi(
        url = this.url,
        mimeType = this.mimeType,
        type = this.type,
        variants = this.variants ?: emptyList(),
        title = this.title,
        description = this.description,
        thumbnail = this.thumbnail,
        authorAvatarUrl = this.authorAvatarUrl,
    )
