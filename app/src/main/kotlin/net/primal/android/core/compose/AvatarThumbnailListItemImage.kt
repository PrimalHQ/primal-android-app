package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.images.AvatarCoilImageLoader
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.theme.AppTheme

@Composable
fun UniversalAvatarThumbnail(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage? = null,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = true,
    legendaryCustomization: LegendaryCustomization? = null,
    fallbackBorderColor: Color = Color.Transparent,
    borderSizeOverride: Dp? = null,
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit = { DefaultAvatarThumbnailPlaceholderListItemImage() },
) {
    val variant = avatarCdnImage?.variants?.minByOrNull { it.width }
    val imageSource = variant?.mediaUrl ?: avatarCdnImage?.sourceUrl

    val borderBrush = if (legendaryCustomization?.avatarGlow == true &&
        legendaryCustomization.legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION
    ) {
        legendaryCustomization.legendaryStyle?.brush
    } else {
        null
    }

    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        source = imageSource,
        hasBorder = hasBorder,
        borderBrush = borderBrush ?: Brush.linearGradient(
            colors = listOf(
                fallbackBorderColor,
                fallbackBorderColor,
            ),
        ),
        borderSize = borderSizeOverride ?: avatarSize.mapAvatarSizeToBorderSize(),
        backgroundColor = backgroundColor,
        onClick = onClick,
        defaultAvatar = defaultAvatar,
    )
}

@Composable
private fun AvatarThumbnailListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderBrush: Brush = Brush.linearGradient(
        listOf(AppTheme.colorScheme.primary, AppTheme.colorScheme.primary),
    ),
    borderSize: Dp = 2.dp,
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit,
) {
    val animatedAvatars = LocalContentDisplaySettings.current.showAnimatedAvatars
    val context = LocalContext.current
    val imageLoader = if (animatedAvatars) {
        AvatarCoilImageLoader.provideImageLoader(context = context)
    } else {
        AvatarCoilImageLoader.provideNoGifsImageLoader(context = context)
    }

    SubcomposeAsyncImage(
        model = source,
        imageLoader = imageLoader,
        modifier = modifier
            .adjustAvatarBackground(
                size = avatarSize,
                hasBorder = hasBorder,
                borderSize = borderSize,
                borderBrush = borderBrush,
            )
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        contentDescription = stringResource(id = R.string.accessibility_profile_image),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor),
            )
        },
        error = { defaultAvatar() },
    )
}

fun Modifier.adjustAvatarBackground(
    size: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderSize: Dp = 2.dp,
    borderBrush: Brush,
): Modifier {
    return if (hasBorder) {
        this
            .size(size + borderSize * 2)
            .border(
                width = borderSize,
                brush = borderBrush,
                shape = CircleShape,
            )
            .padding(borderSize)
            .clip(CircleShape)
    } else {
        this
            .size(size + borderSize * 2)
            .border(
                width = borderSize,
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.Transparent),
                ),
                shape = CircleShape,
            )
            .padding(borderSize)
            .clip(CircleShape)
    }
}

@Suppress("MagicNumber")
private fun Dp.mapAvatarSizeToBorderSize(): Dp =
    when {
        this >= 112.dp -> 4.dp
        this >= 80.dp -> 3.dp
        this >= 32.dp -> 2.dp
        this >= 24.dp -> (1.5).dp
        else -> 1.dp
    }

@Composable
fun DefaultAvatarThumbnailPlaceholderListItemImage(
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor)
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
