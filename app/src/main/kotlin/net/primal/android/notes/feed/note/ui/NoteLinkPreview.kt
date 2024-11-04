package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.GenericLinkIcon
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.extractTLD
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private val ThumbnailWidth = 100.dp
private val ThumbnailHeight = 90.dp

@Composable
fun NoteLinkPreview(
    url: String,
    title: String?,
    thumbnailUrl: String?,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .height(height = ThumbnailHeight)
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt3, shape = AppTheme.shapes.small)
            .border(width = 0.5.dp, color = AppTheme.extraColorScheme.surfaceVariantAlt1, shape = AppTheme.shapes.small)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
    ) {
        if (thumbnailUrl != null) {
            SubcomposeAsyncImage(
                model = thumbnailUrl,
                modifier = Modifier
                    .clip(
                        shape = AppTheme.shapes.small.copy(
                            topEnd = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp),
                        ),
                    )
                    .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
                    .size(width = ThumbnailWidth, height = ThumbnailHeight),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    NoteImageLoadingPlaceholder()
                },
                error = {
                    GenericLinkThumbnailIcon()
                },
            )
        } else {
            GenericLinkThumbnailIcon()
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            if (title != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp),
                    text = title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppTheme.colorScheme.onSurface,
                    style = AppTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val tld = url.extractTLD()
            if (tld != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp),
                    text = tld,
                    maxLines = 1,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GenericLinkThumbnailIcon() {
    Box(
        modifier = Modifier
            .size(width = ThumbnailWidth, height = ThumbnailHeight)
            .clip(
                shape = AppTheme.shapes.small.copy(
                    topEnd = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp),
                ),
            )
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(size = 44.dp),
            imageVector = PrimalIcons.GenericLinkIcon,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
        )
    }
}

@Preview
@Composable
private fun PreviewNoteLinkPreview() {
    PrimalPreview(
        primalTheme = PrimalTheme.Sunrise,
    ) {
        NoteLinkPreview(
            url = "https://action.aclu.org",
            title = "Stop Mass Warrantless Surveillance: End Section 70",
            thumbnailUrl = null,
        )
    }
}
