package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.attachments.domain.calculateImageSize
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.PostImageListItemImage
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.theme.AppTheme

@ExperimentalFoundationApi
@Composable
fun NoteMediaAttachmentsPreview(
    onAttachmentClick: (String) -> Unit,
    imageAttachments: List<NoteAttachmentUi> = emptyList(),
) {
    BoxWithConstraints {
        val imageSizeDp = findImageSize(attachment = imageAttachments.first())
        val imagesCount = imageAttachments.size

        val pagerState = rememberPagerState { imagesCount }
        HorizontalPager(state = pagerState) {
            NoteImageAttachment(
                attachment = imageAttachments[it],
                imageSizeDp = imageSizeDp,
                onClick = {
                    onAttachmentClick(imageAttachments[it].url)
                },
            )
        }

        if (imagesCount > 1) {
            HorizontalPagerIndicator(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                imagesCount = imagesCount,
                currentPage = pagerState.currentPage,
            )
        }
    }
}

@Composable
private fun NoteImageAttachment(
    attachment: NoteAttachmentUi,
    imageSizeDp: DpSize,
    onClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(AppTheme.shapes.medium),
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
        val imageSource = variant?.mediaUrl ?: attachment.url
        PostImageListItemImage(
            source = imageSource,
            modifier = Modifier
                .width(imageSizeDp.width)
                .height(imageSizeDp.height)
                .clickable(onClick = onClick),
        )
    }
}

private const val MAX_SCREEN_HEIGHT_VISIBLE_AREA = 0.77

@Composable
private fun BoxWithConstraintsScope.findImageSize(attachment: NoteAttachmentUi): DpSize {
    val density = LocalDensity.current.density
    val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
    val maxWidth = maxWidth.value.toInt()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    return variant.calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
    )
}
