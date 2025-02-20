package net.primal.android.core.compose.attachment.model

import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.attachments.domain.NoteAttachmentType

data class NoteAttachmentUi(
    val noteId: String,
    val url: String,
    val type: NoteAttachmentType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val authorAvatarUrl: String? = null,
    val position: Int,
)

fun NoteAttachment.asNoteAttachmentUi() =
    NoteAttachmentUi(
        noteId = this.eventId,
        url = this.url,
        mimeType = this.mimeType,
        type = this.type,
        variants = this.variants ?: emptyList(),
        title = this.title,
        description = this.description,
        thumbnailUrl = this.thumbnail,
        authorAvatarUrl = this.authorAvatarUrl,
        position = this.position,
    )

fun NoteAttachmentUi.isMediaAttachment() = type == NoteAttachmentType.Image || type == NoteAttachmentType.Video
