package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.android.theme.AppTheme
import net.primal.domain.links.EventUriType

const val SINGLE_IMAGE = 1
const val TWO_IMAGES = 2
const val THREE_IMAGES = 3
const val FOUR_IMAGES = 4
private val GalleryGapSpace = 1.dp
private val RadiusSize = 8.dp

@ExperimentalFoundationApi
@Composable
fun NoteMediaAttachmentsHorizontalPager(
    modifier: Modifier = Modifier,
    onMediaClick: (MediaClickEvent) -> Unit,
    blossoms: List<String>,
    mediaEventUris: List<EventUriUi> = emptyList(),
) {
    BoxWithConstraints(modifier = modifier) {
        val imageSizeDp = findImageSize(eventUri = mediaEventUris.first())
        val imagesCount = mediaEventUris.size

        when (imagesCount) {
            SINGLE_IMAGE -> {
                SingleImageGallery(
                    mediaEventUris = mediaEventUris,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            TWO_IMAGES -> {
                TwoImageGallery(
                    mediaEventUris = mediaEventUris,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            THREE_IMAGES -> {
                ThreeImageGallery(
                    mediaEventUris = mediaEventUris,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            else -> {
                MultipleImageGallery(
                    mediaEventUri = mediaEventUris,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
        }
    }
}

@Composable
private fun MultipleImageGallery(
    mediaEventUri: List<EventUriUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    val maxDisplayImages = FOUR_IMAGES
    val shapeMatrix = listOf(
        RoundedCornerShape(CornerSize(RadiusSize), CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(RadiusSize), CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp), CornerSize(RadiusSize)),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(RadiusSize), CornerSize(0.dp)),
    )

    val displayedMedia = mediaEventUri.take(maxDisplayImages)
    val hasExtra = mediaEventUri.size > maxDisplayImages
    val extraCount = mediaEventUri.size - maxDisplayImages

    Column(modifier = Modifier.fillMaxWidth()) {
        displayedMedia.chunked(2).forEachIndexed { rowIndex, rowAttachments ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GalleryGapSpace),
            ) {
                rowAttachments.forEachIndexed { index, media ->
                    val position = rowIndex * 2 + index
                    val shape = shapeMatrix.getOrNull(position) ?: RoundedCornerShape(0.dp)
                    val isLastVisible = hasExtra && position == maxDisplayImages - 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(if (rowIndex == 0) Modifier else Modifier.padding(vertical = GalleryGapSpace))
                            .clip(shape),
                    ) {
                        NoteMediaAttachment(
                            modifier = Modifier.fillMaxSize(),
                            mediaEventUri = media,
                            blossoms = blossoms,
                            imageSizeDp = imageSizeDp,
                            onClick = { positionMs ->
                                onMediaClick(
                                    MediaClickEvent(
                                        noteId = media.eventId,
                                        eventUriType = media.type,
                                        mediaUrl = media.url,
                                        positionMs = positionMs,
                                    ),
                                )
                            },
                        )

                        if (isLastVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "+$extraCount",
                                    style = AppTheme.typography.bodyLarge.copy(
                                        fontSize = 48.sp,
                                    ),
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreeImageGallery(
    mediaEventUris: List<EventUriUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        NoteMediaAttachment(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 1f)
                .padding(vertical = GalleryGapSpace)
                .clip(
                    RoundedCornerShape(
                        topStart = CornerSize(RadiusSize),
                        topEnd = CornerSize(RadiusSize),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                ),
            mediaEventUri = mediaEventUris[0],
            blossoms = blossoms,
            imageSizeDp = imageSizeDp,
            onClick = { positionMs ->
                onMediaClick(
                    MediaClickEvent(
                        noteId = mediaEventUris[0].eventId,
                        eventUriType = mediaEventUris[0].type,
                        mediaUrl = mediaEventUris[0].url,
                        positionMs = positionMs,
                    ),
                )
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GalleryGapSpace),
        ) {
            mediaEventUris.drop(1).take(2).forEachIndexed { index, attachment ->
                NoteMediaAttachment(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(
                            if (index == 0) {
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = CornerSize(RadiusSize),
                                    bottomEnd = CornerSize(0.dp),
                                )
                            } else {
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = CornerSize(RadiusSize),
                                )
                            },
                        ),
                    mediaEventUri = attachment,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onClick = { positionMs ->
                        onMediaClick(
                            MediaClickEvent(
                                noteId = attachment.eventId,
                                eventUriType = attachment.type,
                                mediaUrl = attachment.url,
                                positionMs = positionMs,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun TwoImageGallery(
    mediaEventUris: List<EventUriUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GalleryGapSpace),
    ) {
        mediaEventUris.take(2).forEachIndexed { index, attachment ->
            NoteMediaAttachment(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(vertical = GalleryGapSpace)
                    .clip(
                        if (index == 0) {
                            RoundedCornerShape(
                                topStart = CornerSize(RadiusSize),
                                topEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(RadiusSize),
                                bottomEnd = CornerSize(0.dp),
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = CornerSize(0.dp),
                                topEnd = CornerSize(RadiusSize),
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = CornerSize(RadiusSize),
                            )
                        },
                    ),
                mediaEventUri = attachment,
                blossoms = blossoms,
                imageSizeDp = imageSizeDp,
                onClick = { positionMs ->
                    onMediaClick(
                        MediaClickEvent(
                            noteId = attachment.eventId,
                            eventUriType = attachment.type,
                            mediaUrl = attachment.url,
                            positionMs = positionMs,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun SingleImageGallery(
    mediaEventUris: List<EventUriUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    val mediaEventUri = mediaEventUris.first()
    NoteMediaAttachment(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(RadiusSize)),
        mediaEventUri = mediaEventUri,
        blossoms = blossoms,
        imageSizeDp = imageSizeDp,
        onClick = { positionMs ->
            onMediaClick(
                MediaClickEvent(
                    noteId = mediaEventUri.eventId,
                    eventUriType = mediaEventUri.type,
                    mediaUrl = mediaEventUri.url,
                    positionMs = positionMs,
                ),
            )
        },
    )
}

@Composable
private fun NoteMediaAttachment(
    mediaEventUri: EventUriUi,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (mediaEventUri.type) {
            EventUriType.Video -> {
                NoteAttachmentVideoPreview(
                    eventUri = mediaEventUri,
                    onVideoClick = { positionMs -> onClick(positionMs) },
                    modifier = Modifier
                        .width(imageSizeDp.width)
                        .height(imageSizeDp.height),
                )
            }

            else -> {
                NoteAttachmentImagePreview(
                    attachment = mediaEventUri,
                    blossoms = blossoms,
                    maxWidth = this.maxWidth,
                    modifier = Modifier
                        .width(imageSizeDp.width)
                        .height(imageSizeDp.height)
                        .clickable { onClick(0L) },
                )
            }
        }
    }
}
