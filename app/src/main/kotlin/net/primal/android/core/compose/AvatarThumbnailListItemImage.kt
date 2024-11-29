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
import net.primal.android.theme.AppTheme

@Composable
fun UniversalAvatarThumbnail(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage? = null,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = true,
    hasGlowOverride: Boolean? = null,
    fallbackBorderColor: Color = Color.Transparent,
    legendaryCustomization: LegendaryCustomization? = null,
    borderSize: Dp = 2.dp,
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit = { DefaultAvatarThumbnailPlaceholderListItemImage() },
) {
    val variant = avatarCdnImage?.variants?.minByOrNull { it.width }
    val imageSource = variant?.mediaUrl ?: avatarCdnImage?.sourceUrl

    val borderBrush = if (legendaryCustomization?.avatarGlow == true) {
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
        hasGlow = (hasGlowOverride ?: legendaryCustomization?.avatarGlow) == true,
        borderSize = borderSize,
        backgroundColor = backgroundColor,
        onClick = onClick,
        defaultAvatar = defaultAvatar,
    )
}

@Deprecated(
    message = "This component is deprecated in favor of UniversalAvatarThumbnail",
    replaceWith = ReplaceWith("UniversalAvatarThumbnail"),
)
@Composable
fun AvatarThumbnail(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage? = null,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderColor: Color = AppTheme.colorScheme.primary,
    borderSize: Dp = 2.dp,
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit = { DefaultAvatarThumbnailPlaceholderListItemImage() },
) {
    AvatarThumbnailCustomBorder(
        modifier = modifier,
        avatarCdnImage = avatarCdnImage,
        avatarSize = avatarSize,
        hasBorder = hasBorder,
        borderBrush = Brush.linearGradient(listOf(borderColor, borderColor)),
        borderSize = borderSize,
        backgroundColor = backgroundColor,
        onClick = onClick,
        defaultAvatar = defaultAvatar,
    )
}

@Deprecated(
    message = "This component is deprecated in favor of UniversalAvatarThumbnail",
    replaceWith = ReplaceWith("UniversalAvatarThumbnail"),
)
@Composable
fun AvatarThumbnailCustomBorder(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage? = null,
    avatarSize: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderBrush: Brush = Brush.linearGradient(
        listOf(AppTheme.colorScheme.primary, AppTheme.colorScheme.primary),
    ),
    borderSize: Dp = 2.dp,
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit = { DefaultAvatarThumbnailPlaceholderListItemImage() },
) {
    val variant = avatarCdnImage?.variants?.minByOrNull { it.width }
    val imageSource = variant?.mediaUrl ?: avatarCdnImage?.sourceUrl
    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        source = imageSource,
        hasBorder = hasBorder,
        borderBrush = borderBrush,
        borderSize = borderSize,
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
    hasGlow: Boolean = false,
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
            .size(size + borderSize)
            .border(
                width = borderSize,
                brush = borderBrush,
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
