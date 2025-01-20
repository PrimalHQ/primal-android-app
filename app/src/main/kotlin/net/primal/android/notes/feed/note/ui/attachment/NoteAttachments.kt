package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.isMediaAttachment
import net.primal.android.notes.feed.note.ui.NoteLinkPreview
import net.primal.android.notes.feed.note.ui.NoteVideoLinkPreview
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent

@ExperimentalFoundationApi
@Composable
fun NoteAttachments(
    modifier: Modifier = Modifier,
    attachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    onUrlClick: ((mediaUrl: String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
) {
    val mediaAttachments = attachments.filter { it.isMediaAttachment() }
    if (mediaAttachments.isNotEmpty()) {
        NoteMediaAttachmentsHorizontalPager(
            modifier = modifier,
            mediaAttachments = mediaAttachments,
            blossoms = blossoms,
            onMediaClick = {
                when (it.noteAttachmentType) {
                    NoteAttachmentType.Image, NoteAttachmentType.Video -> onMediaClick?.invoke(it)
                    else -> onUrlClick?.invoke(it.mediaUrl)
                }
            },
        )
    }

    val linkAttachments = attachments.filterNot { it.isMediaAttachment() }
    linkAttachments.forEach { attachment ->
        BoxWithConstraints(modifier = modifier) {
            when (attachment.type) {
                NoteAttachmentType.YouTube,
                NoteAttachmentType.Rumble,
                    -> {
                    val thumbnailImageSizeDp = findImageSize(attachment = attachment)
                    NoteVideoLinkPreview(
                        url = attachment.url,
                        title = attachment.title,
                        thumbnailUrl = attachment.thumbnailUrl,
                        thumbnailImageSize = thumbnailImageSizeDp,
                        type = attachment.type,
                        onClick = if (onUrlClick != null) {
                            { onUrlClick.invoke(attachment.url) }
                        } else {
                            null
                        },
                    )
                }

                else -> {
                    if (!attachment.title.isNullOrBlank()) {
                        NoteLinkPreview(
                            url = attachment.url,
                            title = attachment.title,
                            thumbnailUrl = attachment.thumbnailUrl,
                            onClick = if (onUrlClick != null) {
                                { onUrlClick.invoke(attachment.url) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}
