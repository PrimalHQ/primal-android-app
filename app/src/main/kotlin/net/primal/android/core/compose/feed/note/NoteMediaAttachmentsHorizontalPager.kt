package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import net.primal.android.theme.AppTheme

@ExperimentalFoundationApi
@Composable
fun NoteMediaAttachmentsHorizontalPager(
    onAttachmentClick: (NoteAttachmentUi, String) -> Unit,
    mediaAttachments: List<NoteAttachmentUi> = emptyList(),
) {
    BoxWithConstraints {
        val imageSizeDp = findImageSize(attachment = mediaAttachments.first())
        val imagesCount = mediaAttachments.size

        val pagerState = rememberPagerState { imagesCount }

        if (imagesCount == 1) {
            val attachment = mediaAttachments.first()
            NoteMediaAttachment(
                attachment = attachment,
                imageSizeDp = imageSizeDp,
                onClick = { onAttachmentClick(attachment, attachment.url) },
            )
        } else {
            HorizontalPager(state = pagerState) {
                val attachment = mediaAttachments[it]
                NoteMediaAttachment(
                    attachment = attachment,
                    imageSizeDp = imageSizeDp,
                    onClick = { onAttachmentClick(attachment, attachment.url) },
                )
            }
        }

        if (imagesCount > 1) {
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
private fun NoteMediaAttachment(
    attachment: NoteAttachmentUi,
    imageSizeDp: DpSize,
    onClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(AppTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        when (attachment.type) {
            NoteAttachmentType.Video -> {
                NoteAttachmentVideoPreview(
                    attachment = attachment,
                    onVideoClick = { positionMs ->
                        // Open media gallery
                    },
                    modifier = Modifier
                        .width(imageSizeDp.width)
                        .height(imageSizeDp.height),
                )
            }

            else -> {
                NoteAttachmentImagePreview(
                    attachment = attachment,
                    maxWidth = this.maxWidth,
                    modifier = Modifier
                        .width(imageSizeDp.width)
                        .height(imageSizeDp.height)
                        .clickable(onClick = onClick),
                )
            }
        }
    }
}
