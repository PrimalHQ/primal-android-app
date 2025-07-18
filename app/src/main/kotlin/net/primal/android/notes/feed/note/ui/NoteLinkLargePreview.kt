package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalAsyncImage
import net.primal.android.theme.AppTheme
import net.primal.core.utils.extractTLD

@Composable
fun NoteLinkLargePreview(
    url: String,
    title: String?,
    description: String?,
    thumbnailUrl: String?,
    thumbnailImageSize: DpSize,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.small,
            )
            .border(
                width = Dp.Hairline,
                color = AppTheme.colorScheme.outline,
                shape = AppTheme.shapes.small,
            )
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .clip(AppTheme.shapes.small),
    ) {
        if (thumbnailUrl != null) {
            PrimalAsyncImage(
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
                contentScale = ContentScale.FillHeight,
            )
        }

        val tld = url.extractTLD()
        if (tld != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = tld,
                maxLines = 1,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodySmall,
            )
        }

        if (title != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (description != null) 4.dp else 0.dp),
                text = title,
                maxLines = 2,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyMedium,
            )
        }

        if (description != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp),
                text = description,
                maxLines = 4,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
