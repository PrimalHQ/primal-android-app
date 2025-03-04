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
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.android.theme.AppTheme

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
    mediaAttachments: List<NoteAttachmentUi> = emptyList(),
) {
    BoxWithConstraints(modifier = modifier) {
        val imageSizeDp = findImageSize(attachment = mediaAttachments.first())
        val imagesCount = mediaAttachments.size

        val pagerState = rememberPagerState { imagesCount }

        when (imagesCount) {
            SINGLE_IMAGE -> {
                SingleImageGallery(
                    mediaAttachments = mediaAttachments,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            TWO_IMAGES -> {
                TwoImageGallery(
                    mediaAttachments = mediaAttachments,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            THREE_IMAGES -> {
                ThreeImageGallery(
                    mediaAttachments = mediaAttachments,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            FOUR_IMAGES -> {
                FourImageGallery(
                    mediaAttachments = mediaAttachments,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onMediaClick = onMediaClick,
                )
            }
            else -> {
                HorizontalPager(state = pagerState, pageSpacing = 12.dp) {
                    val attachment = mediaAttachments[it]
                    NoteMediaAttachment(
                        modifier = Modifier.clip(AppTheme.shapes.medium),
                        attachment = attachment,
                        blossoms = blossoms,
                        imageSizeDp = imageSizeDp,
                        onClick = { positionMs ->
                            onMediaClick(
                                MediaClickEvent(
                                    noteId = attachment.noteId,
                                    noteAttachmentType = attachment.type,
                                    mediaUrl = attachment.url,
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
    mediaAttachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    val shapeMatrix = listOf(
        RoundedCornerShape(AppTheme.shapes.medium.topStart, CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), AppTheme.shapes.medium.topEnd, CornerSize(0.dp), CornerSize(0.dp)),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(0.dp), AppTheme.shapes.medium.bottomEnd),
        RoundedCornerShape(CornerSize(0.dp), CornerSize(0.dp), AppTheme.shapes.medium.bottomStart, CornerSize(0.dp)),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        mediaAttachments.chunked(2).forEachIndexed { rowIndex, rowAttachments ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowAttachments.forEachIndexed { index, attachment ->
                    NoteMediaAttachment(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(shapeMatrix[rowIndex * 2 + index]),
                        attachment = attachment,
                        blossoms = blossoms,
                        imageSizeDp = imageSizeDp,
                        onClick = { positionMs ->
                            onMediaClick(
                                MediaClickEvent(
                                    noteId = attachment.noteId,
                                    noteAttachmentType = attachment.type,
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
}

@Composable
private fun ThreeImageGallery(
    mediaAttachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        NoteMediaAttachment(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 1f)
                .clip(
                    RoundedCornerShape(
                        topStart = AppTheme.shapes.medium.topStart,
                        topEnd = AppTheme.shapes.medium.topEnd,
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                ),
            attachment = mediaAttachments[0],
            blossoms = blossoms,
            imageSizeDp = imageSizeDp,
            onClick = { positionMs ->
                onMediaClick(
                    MediaClickEvent(
                        noteId = mediaAttachments[0].noteId,
                        noteAttachmentType = mediaAttachments[0].type,
                        mediaUrl = mediaAttachments[0].url,
                        positionMs = positionMs,
                    ),
                )
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            mediaAttachments.drop(1).take(2).forEachIndexed { index, attachment ->
                NoteMediaAttachment(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(
                            if (index == 0) {
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = AppTheme.shapes.medium.bottomStart,
                                    bottomEnd = CornerSize(0.dp),
                                )
                            } else {
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = AppTheme.shapes.medium.bottomEnd,
                                )
                            },
                        ),
                    attachment = attachment,
                    blossoms = blossoms,
                    imageSizeDp = imageSizeDp,
                    onClick = { positionMs ->
                        onMediaClick(
                            MediaClickEvent(
                                noteId = attachment.noteId,
                                noteAttachmentType = attachment.type,
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
    mediaAttachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        mediaAttachments.take(2).forEachIndexed { index, attachment ->
            NoteMediaAttachment(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(
                        if (index == 0) {
                            RoundedCornerShape(
                                topStart = AppTheme.shapes.medium.topStart,
                                topEnd = CornerSize(0.dp),
                                bottomStart = AppTheme.shapes.medium.bottomStart,
                                bottomEnd = CornerSize(0.dp),
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = CornerSize(0.dp),
                                topEnd = AppTheme.shapes.medium.topEnd,
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = AppTheme.shapes.medium.bottomEnd,
                            )
                        },
                    ),
                attachment = attachment,
                blossoms = blossoms,
                imageSizeDp = imageSizeDp,
                onClick = { positionMs ->
                    onMediaClick(
                        MediaClickEvent(
                            noteId = attachment.noteId,
                            noteAttachmentType = attachment.type,
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
    mediaAttachments: List<NoteAttachmentUi>,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onMediaClick: (MediaClickEvent) -> Unit,
) {
    val attachment = mediaAttachments.first()
    NoteMediaAttachment(
        modifier = Modifier.clip(AppTheme.shapes.medium),
        attachment = attachment,
        blossoms = blossoms,
        imageSizeDp = imageSizeDp,
        onClick = { positionMs ->
            onMediaClick(
                MediaClickEvent(
                    noteId = attachment.noteId,
                    noteAttachmentType = attachment.type,
                    mediaUrl = attachment.url,
                    positionMs = positionMs,
                ),
            )
        },
    )
}

@Composable
private fun NoteMediaAttachment(
    attachment: NoteAttachmentUi,
    blossoms: List<String>,
    imageSizeDp: DpSize,
    onClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (attachment.type) {
            NoteAttachmentType.Video -> {
                NoteAttachmentVideoPreview(
                    attachment = attachment,
                    onVideoClick = { positionMs -> onClick(positionMs) },
                    modifier = Modifier
                        .width(imageSizeDp.width)
                        .height(imageSizeDp.height),
                )
            }

            else -> {
                NoteAttachmentImagePreview(
                    attachment = attachment,
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
