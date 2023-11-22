package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.isMediaAttachment

@ExperimentalFoundationApi
@Composable
fun NoteAttachments(
    noteId: String,
    attachments: List<NoteAttachmentUi>,
    onUrlClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
) {
    val mediaAttachments = attachments.filter { it.isMediaAttachment() }
    val linkAttachments = attachments.filterNot { it.isMediaAttachment() }
    when {
        mediaAttachments.isNotEmpty() -> {
            NoteMediaAttachmentsHorizontalPager(
                mediaAttachments = mediaAttachments,
                onAttachmentClick = { attachment, mediaUrl ->
                    when (attachment.type) {
                        NoteAttachmentType.Image -> onMediaClick(noteId, mediaUrl)
                        else -> onUrlClick(mediaUrl)
                    }
                },
            )
        }

        linkAttachments.size == 1 -> {
            val attachment = linkAttachments.first()
            if (!attachment.title.isNullOrBlank() || !attachment.description.isNullOrBlank()) {
                NoteLinkPreview(
                    url = attachment.url,
                    title = attachment.title,
                    description = attachment.description,
                    thumbnailUrl = attachment.thumbnailUrl,
                    onClick = { onUrlClick(attachment.url) },
                )
            }
        }
    }
}
