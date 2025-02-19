package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.isMediaAttachment
import net.primal.android.notes.feed.note.ui.NoteAudioSpotifyLinkPreview
import net.primal.android.notes.feed.note.ui.NoteAudioTidalLinkPreview
import net.primal.android.notes.feed.note.ui.NoteLinkLargePreview
import net.primal.android.notes.feed.note.ui.NoteLinkPreview
import net.primal.android.notes.feed.note.ui.NoteVideoLinkPreview
import net.primal.android.notes.feed.note.ui.NoteYouTubeLinkPreview
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent

@ExperimentalFoundationApi
@Composable
fun NoteAttachments(
    modifier: Modifier = Modifier,
    attachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    expanded: Boolean,
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

    attachments
        .filterNot { it.isMediaAttachment() }
        .take(n = if (!expanded) 2 else Int.MAX_VALUE)
        .filter {
            when (it.type) {
                NoteAttachmentType.YouTube, NoteAttachmentType.Rumble -> {
                    it.title != null || it.thumbnailUrl != null
                }
                else -> true
            }
        }
        .forEach { attachment ->
            NoteLinkAttachment(
                modifier = modifier,
                attachment = attachment,
                onUrlClick = onUrlClick,
            )
        }
}

@Composable
private fun NoteLinkAttachment(
    modifier: Modifier,
    attachment: NoteAttachmentUi,
    onUrlClick: ((mediaUrl: String) -> Unit)?,
) {
    BoxWithConstraints(modifier = modifier) {
        val thumbnailImageSizeDp = findImageSize(attachment = attachment)
        when (attachment.type) {
            NoteAttachmentType.YouTube -> {
                NoteYouTubeLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    url = attachment.url,
                    title = attachment.title,
                    thumbnailUrl = attachment.thumbnailUrl,
                    thumbnailImageSizeDp = thumbnailImageSizeDp,
                    onClick = { onUrlClick?.invoke(attachment.url) },
                )
            }

            NoteAttachmentType.Rumble -> {
                NoteVideoLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    title = attachment.title,
                    thumbnailUrl = attachment.thumbnailUrl,
                    thumbnailImageSize = thumbnailImageSizeDp,
                    type = attachment.type,
                    onClick = if (onUrlClick != null) {
                        { onUrlClick(attachment.url) }
                    } else {
                        null
                    },
                )
            }

            NoteAttachmentType.Spotify -> {
                NoteAudioSpotifyLinkPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    url = attachment.url,
                    title = attachment.title,
                    description = attachment.description,
                    thumbnailUrl = attachment.thumbnailUrl,
                    onPlayClick = { onUrlClick?.invoke(attachment.url) },
                )
            }

            NoteAttachmentType.Tidal -> {
                NoteAudioTidalLinkPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    url = attachment.url,
                    title = attachment.title,
                    description = attachment.description,
                    thumbnailUrl = attachment.thumbnailUrl,
                )
            }

            NoteAttachmentType.GitHub -> {
                NoteLinkLargePreview(
                    url = attachment.url,
                    title = attachment.title,
                    thumbnailUrl = attachment.thumbnailUrl,
                    onClick = { onUrlClick?.invoke(attachment.url) },
                    description = attachment.description,
                    thumbnailImageSize = thumbnailImageSizeDp,
                )
            }

            else -> if (!attachment.title.isNullOrBlank()) {
                NoteLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
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
