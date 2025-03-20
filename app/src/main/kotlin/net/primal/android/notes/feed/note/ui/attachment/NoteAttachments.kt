package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.attachment.model.isMediaUri
import net.primal.android.notes.feed.note.ui.NoteAudioSpotifyLinkPreview
import net.primal.android.notes.feed.note.ui.NoteAudioTidalLinkPreview
import net.primal.android.notes.feed.note.ui.NoteLinkLargePreview
import net.primal.android.notes.feed.note.ui.NoteLinkPreview
import net.primal.android.notes.feed.note.ui.NoteVideoLinkPreview
import net.primal.android.notes.feed.note.ui.NoteYouTubeLinkPreview
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.domain.EventUriType

@ExperimentalFoundationApi
@Composable
fun NoteAttachments(
    modifier: Modifier = Modifier,
    eventUris: List<EventUriUi>,
    blossoms: List<String>,
    expanded: Boolean,
    onUrlClick: ((mediaUrl: String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
) {
    val mediaAttachments = eventUris.filter { it.isMediaUri() }
    if (mediaAttachments.isNotEmpty()) {
        NoteMediaAttachmentsHorizontalPager(
            modifier = modifier,
            mediaEventUris = mediaAttachments,
            blossoms = blossoms,
            onMediaClick = {
                when (it.eventUriType) {
                    EventUriType.Image, EventUriType.Video -> onMediaClick?.invoke(it)
                    else -> onUrlClick?.invoke(it.mediaUrl)
                }
            },
        )
    }

    eventUris
        .filterNot { it.isMediaUri() }
        .take(n = if (!expanded) 2 else Int.MAX_VALUE)
        .filter {
            when (it.type) {
                EventUriType.YouTube, EventUriType.Rumble, EventUriType.Spotify -> {
                    it.title != null || it.thumbnailUrl != null
                }
                else -> true
            }
        }
        .forEach { attachment ->
            NoteLinkAttachment(
                modifier = modifier,
                eventUri = attachment,
                onUrlClick = onUrlClick,
            )
        }
}

@Composable
private fun NoteLinkAttachment(
    modifier: Modifier,
    eventUri: EventUriUi,
    onUrlClick: ((mediaUrl: String) -> Unit)?,
) {
    BoxWithConstraints(modifier = modifier) {
        val thumbnailImageSizeDp = findImageSize(eventUri = eventUri)
        when (eventUri.type) {
            EventUriType.YouTube -> {
                NoteYouTubeLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    url = eventUri.url,
                    title = eventUri.title,
                    thumbnailUrl = eventUri.thumbnailUrl,
                    thumbnailImageSizeDp = thumbnailImageSizeDp,
                    onClick = { onUrlClick?.invoke(eventUri.url) },
                )
            }

            EventUriType.Rumble -> {
                NoteVideoLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    title = eventUri.title,
                    thumbnailUrl = eventUri.thumbnailUrl,
                    thumbnailImageSize = thumbnailImageSizeDp,
                    type = eventUri.type,
                    onClick = if (onUrlClick != null) {
                        { onUrlClick(eventUri.url) }
                    } else {
                        null
                    },
                )
            }

            EventUriType.Spotify -> {
                NoteAudioSpotifyLinkPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    url = eventUri.url,
                    title = eventUri.title,
                    description = eventUri.description,
                    thumbnailUrl = eventUri.thumbnailUrl,
                    onPlayClick = { onUrlClick?.invoke(eventUri.url) },
                )
            }

            EventUriType.Tidal -> {
                NoteAudioTidalLinkPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    url = eventUri.url,
                    title = eventUri.title,
                    description = eventUri.description,
                    thumbnailUrl = eventUri.thumbnailUrl,
                )
            }

            EventUriType.GitHub -> {
                NoteLinkLargePreview(
                    url = eventUri.url,
                    title = eventUri.title,
                    thumbnailUrl = eventUri.thumbnailUrl,
                    onClick = { onUrlClick?.invoke(eventUri.url) },
                    description = eventUri.description,
                    thumbnailImageSize = DpSize(width = maxWidth, height = maxWidth / 2),
                )
            }

            else -> if (!eventUri.title.isNullOrBlank()) {
                NoteLinkPreview(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    url = eventUri.url,
                    title = eventUri.title,
                    thumbnailUrl = eventUri.thumbnailUrl,
                    onClick = if (onUrlClick != null) {
                        { onUrlClick.invoke(eventUri.url) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
