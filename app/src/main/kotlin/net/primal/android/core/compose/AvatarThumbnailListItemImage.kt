package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.theme.AppTheme

@Composable
fun AvatarThumbnail(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage? = null,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderColor: Color = AppTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    val variant = avatarCdnImage?.variants?.minByOrNull { it.width }
    val imageSource = variant?.mediaUrl ?: avatarCdnImage?.sourceUrl
    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        source = imageSource,
        hasBorder = hasBorder,
        borderColor = borderColor,
        onClick = onClick,
    )
}

@Composable
private fun AvatarThumbnailListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderColor: Color = AppTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = source,
        modifier = modifier
            .adjustAvatarBackground(
                size = avatarSize,
                hasBorder = hasBorder,
                borderColor = borderColor,
            )
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1),
            )
        },
        error = { DefaultAvatarThumbnailPlaceholderListItemImage() },
    )
}

fun Modifier.adjustAvatarBackground(
    size: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderColor: Color,
): Modifier {
    return if (hasBorder) {
        this
            .size(size + 2.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .clip(CircleShape)
    } else {
        this
            .size(size)
            .clip(CircleShape)
    }
}

@Composable
private fun DefaultAvatarThumbnailPlaceholderListItemImage() {
    Box(
        modifier = Modifier
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PrimalIcons.AvatarDefault,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = LocalContentColor.current,
        )
    }
}
