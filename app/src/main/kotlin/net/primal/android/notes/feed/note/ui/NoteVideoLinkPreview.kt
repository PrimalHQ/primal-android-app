package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.notes.feed.note.ui.attachment.PlayButton
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.EventUriType

@Composable
fun NoteVideoLinkPreview(
    title: String?,
    thumbnailUrl: String?,
    thumbnailImageSize: DpSize,
    type: EventUriType,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.small,
            )
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
    ) {
        if (thumbnailUrl != null) {
            VideoThumbnailImage(
                thumbnailUrl = thumbnailUrl,
                thumbnailImageSize = thumbnailImageSize,
                loading = loading,
                onClick = onClick,
            )
        }

        TopLevelDomainText(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            eventUriType = type,
        )

        if (title != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp),
                text = title,
                maxLines = 2,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyMedium,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun VideoThumbnailImage(
    thumbnailUrl: String?,
    thumbnailImageSize: DpSize,
    loading: Boolean,
    onClick: (() -> Unit)?,
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            modifier = Modifier
                .clip(
                    shape = AppTheme.shapes.small.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                )
                .width(thumbnailImageSize.width)
                .height(thumbnailImageSize.height),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            loading = { NoteImageLoadingPlaceholder() },
        )

        PlayButton(
            loading = loading,
            onClick = onClick,
        )
    }
}

@Preview
@Composable
private fun PreviewNoteVideoLinkPreview() {
    PrimalPreview(
        primalTheme = PrimalTheme.Sunrise,
    ) {
        NoteVideoLinkPreview(
            title = "Stop Mass Warrantless Surveillance: End Section 70",
            thumbnailUrl = "",
            type = EventUriType.YouTube,
            thumbnailImageSize = DpSize(width = 480.dp, height = 256.dp),
        )
    }
}
