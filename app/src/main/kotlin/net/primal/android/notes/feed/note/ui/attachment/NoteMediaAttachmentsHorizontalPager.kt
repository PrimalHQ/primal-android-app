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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.android.theme.AppTheme
import net.primal.domain.links.EventUriType

const val SINGLE_IMAGE = 1
const val TWO_IMAGES = 2
const val THREE_IMAGES = 3
const val FOUR_IMAGES = 4

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
        val pagerState = rememberPagerState { imagesCount }

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
            FOUR_IMAGES -> {
                FourImageGallery(
                    mediaEventUri = mediaEventUris,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            else -> {
                HorizontalPager(state = pagerState, pageSpacing = 12.dp) {
                    val mediaUri = mediaEventUris[it]
                    NoteMediaAttachment(
                        modifier = Modifier.padding(vertical = 4.dp).clip(AppTheme.shapes.large),
                        mediaEventUri = mediaUri,
                        blossoms = blossoms,
                        imageSizeDp = imageSizeDp,
                        onClick = { positionMs ->
                            onMediaClick(
                                MediaClickEvent(
                                    noteId = mediaUri.eventId,
                                    eventUriType = mediaUri.type,
                                    mediaUrl = mediaUri.url,
                                    positionMs = positionMs,
                                ),
                            )
                        },
                    )
                }
            }
        }

        if (imagesCount > FOUR_IMAGES) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(color = Color.Black.copy(alpha = 0.21f), shape = AppTheme.shapes.large)
                    .padding(horizontal = 8.dp),
            ) {
                HorizontalPagerIndicator(
                    modifier = Modifier.padding(vertical = 4.dp),
                    pagesCount = imagesCount,
                    currentPage = pagerState.currentPage,
                    predecessorsColor = Color.White,
                    successorsColor = Color.White,
                )
            }
        }
    }
}

@Composable
private fun FourImageGallery(
    mediaEventUri: List<EventUriUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    val shapeMatrix = listOf(
        RoundedCornerShape(AppTheme.shapes.large.topStart, CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), AppTheme.shapes.large.topEnd, CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp), AppTheme.shapes.large.bottomEnd),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), AppTheme.shapes.large.bottomStart, CornerSize(0.dp)),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        mediaEventUri.chunked(2).forEachIndexed { rowIndex, rowAttachments ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                rowAttachments.forEachIndexed { index, mediaEventUri ->
                    NoteMediaAttachment(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(if (rowIndex == 0) Modifier.padding(0.dp) else Modifier.padding(vertical = 1.dp))
                            .clip(shapeMatrix[rowIndex * 2 + index]),
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
                .padding(vertical = 1.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = AppTheme.shapes.large.topStart,
                        topEnd = AppTheme.shapes.large.topEnd,
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
            horizontalArrangement = Arrangement.spacedBy(1.dp),
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
                                    bottomStart = AppTheme.shapes.large.bottomStart,
                                    bottomEnd = CornerSize(0.dp),
                                )
                            } else {
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = AppTheme.shapes.large.bottomEnd,
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
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        mediaEventUris.take(2).forEachIndexed { index, attachment ->
            NoteMediaAttachment(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(vertical = 1.dp)
                    .clip(
                        if (index == 0) {
                            RoundedCornerShape(
                                topStart = AppTheme.shapes.large.topStart,
                                topEnd = CornerSize(0.dp),
                                bottomStart = AppTheme.shapes.large.bottomStart,
                                bottomEnd = CornerSize(0.dp),
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = CornerSize(0.dp),
                                topEnd = AppTheme.shapes.large.topEnd,
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = AppTheme.shapes.large.bottomEnd,
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
            .clip(AppTheme.shapes.large),
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
