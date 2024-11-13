package net.primal.android.premium.manage.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.utils.ifNotNull
import net.primal.android.core.utils.toGigaBytes
import net.primal.android.theme.AppTheme

val IMAGES_COLOR = Color(0xFFBC1870)
val VIDEOS_COLOR = Color(0xFF0090F8)
val OTHER_COLOR = Color(0xFFFF9F2F)

@Composable
fun UsedStorageBreakdown(
    modifier: Modifier = Modifier,
    usedStorageInBytes: Long,
    maxStorageInBytes: Long,
    imagesInBytes: Long?,
    videosInBytes: Long?,
    otherInBytes: Long?,
    calculating: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UsedStorageText(
            usedStorageInGigaBytes = usedStorageInBytes.toGigaBytes(),
            maxStorageInGigaBytes = maxStorageInBytes.toGigaBytes(),
        )

        if (calculating) {
            CalculatingBar(usedStorage = usedStorageInBytes, maxStorage = maxStorageInBytes)
            ColorIndicator(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                text = stringResource(id = R.string.premium_media_management_calculating),
            )
        } else {
            ifNotNull(imagesInBytes, videosInBytes, otherInBytes) { images, videos, other ->
                ActualBar(
                    usedStorageInBytes = usedStorageInBytes,
                    maxStorageInBytes = maxStorageInBytes,
                    imagesInBytes = images,
                    videosInBytes = videos,
                    otherInBytes = other,
                )
                MediaIndicators()
            }
        }
    }
}

@Composable
private fun ActualBar(
    modifier: Modifier = Modifier,
    usedStorageInBytes: Long,
    maxStorageInBytes: Long,
    imagesInBytes: Long,
    videosInBytes: Long,
    otherInBytes: Long,
) {
    BoxWithConstraints(
        modifier = Modifier
            .clipToBounds()
            .fillMaxWidth()
            .height(32.dp)
            .clip(AppTheme.shapes.extraSmall)
            .background(AppTheme.colorScheme.outline),
    ) {
        val maxWidth = this.maxWidth
        val imagesWidth = maxWidth * (imagesInBytes / maxStorageInBytes.toFloat())
        val videosWidth = maxWidth * (videosInBytes / maxStorageInBytes.toFloat())
        val otherWidth = maxWidth * (otherInBytes / maxStorageInBytes.toFloat())
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                BoxBar(
                    background = IMAGES_COLOR,
                    width = imagesWidth,
                )
                BoxBar(
                    background = VIDEOS_COLOR,
                    width = videosWidth,
                )
                BoxBar(
                    background = OTHER_COLOR,
                    width = otherWidth,
                )
            }
            FreeStorageText(
                usedStorageInBytes = usedStorageInBytes,
                maxStorageInBytes = maxStorageInBytes,
            )
        }
    }
}

@Composable
private fun MediaIndicators() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorIndicator(
            color = IMAGES_COLOR,
            text = stringResource(id = R.string.premium_media_management_images),
        )
        ColorIndicator(
            color = VIDEOS_COLOR,
            text = stringResource(id = R.string.premium_media_management_videos),
        )
        ColorIndicator(
            color = OTHER_COLOR,
            text = stringResource(id = R.string.premium_media_management_other),
        )
    }
}

@Composable
private fun CalculatingBar(
    modifier: Modifier = Modifier,
    usedStorage: Long,
    maxStorage: Long,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier = modifier
                .clipToBounds()
                .height(32.dp)
                .fillMaxWidth()
                .clip(AppTheme.shapes.extraSmall)
                .background(AppTheme.colorScheme.outline),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth(usedStorage / maxStorage.toFloat())
                    .height(32.dp)
                    .background(AppTheme.extraColorScheme.onSurfaceVariantAlt3),
            )
        }
        FreeStorageText(
            usedStorageInBytes = usedStorage,
            maxStorageInBytes = maxStorage,
        )
    }
}

@Composable
private fun FreeStorageText(
    modifier: Modifier = Modifier,
    usedStorageInBytes: Long,
    maxStorageInBytes: Long,
) {
    Text(
        modifier = modifier.padding(end = 8.dp),
        text = stringResource(
            id = R.string.premium_media_management_free_storage,
            maxStorageInBytes.toGigaBytes() - usedStorageInBytes.toGigaBytes(),
        ),
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        style = AppTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun ColorIndicator(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(percent = 100))
                .background(color),
        )
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun BoxBar(
    modifier: Modifier = Modifier,
    background: Color,
    width: Dp,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(32.dp)
            .background(background),
    )
}

@Composable
private fun UsedStorageText(usedStorageInGigaBytes: Float, maxStorageInGigaBytes: Float) {
    Text(
        text = stringResource(
            id = R.string.premium_media_management_used_storage,
            usedStorageInGigaBytes,
            maxStorageInGigaBytes,
        ),
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        style = AppTheme.typography.bodyLarge,
    )
}
