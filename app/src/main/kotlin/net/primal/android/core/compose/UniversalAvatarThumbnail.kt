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

    val hasLegendBorder = legendaryCustomization?.avatarGlow == true &&
        legendaryCustomization.legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION

    val borderBrush = if (hasLegendBorder) {
        legendaryCustomization?.legendaryStyle?.brush
    } else {
        null
    }

    val totalBorderSize = avatarSize.resolveOuterBorderSizeFromAvatarSize() +
        avatarSize.resolveInnerBorderSizeFromAvatarSize()

    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        source = imageSource,
        hasOuterBorder = hasBorder && avatarSize > 0.dp,
        hasInnerBorder = hasLegendBorder && avatarSize > 0.dp,
        borderBrush = borderBrush ?: Brush.linearGradient(
            colors = listOf(
                fallbackBorderColor,
                fallbackBorderColor,
            ),
        ),
        totalBorderSize = borderSizeOverride ?: totalBorderSize,
        backgroundColor = backgroundColor,
        onClick = onClick,
        defaultAvatar = defaultAvatar,
    )
}

@Composable
private fun defaultBorderBrush() =
    Brush.linearGradient(
        listOf(AppTheme.colorScheme.primary, AppTheme.colorScheme.primary),
    )

@Composable
private fun transparentBorderBrush() =
    Brush.linearGradient(
        listOf(Color.Transparent, Color.Transparent),
    )

@Composable
private fun AvatarThumbnailListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    totalBorderSize: Dp = 2.dp,
    hasOuterBorder: Boolean = false,
    hasInnerBorder: Boolean = false,
    borderBrush: Brush = defaultBorderBrush(),
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
                avatarSize = avatarSize,
                totalBorderSize = totalBorderSize,
                borderBrush = borderBrush,
                hasOuterBorder = hasOuterBorder,
                hasInnerBorder = hasInnerBorder,
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

@Composable
fun Modifier.adjustAvatarBackground(
    avatarSize: Dp,
    totalBorderSize: Dp,
    borderBrush: Brush,
    hasOuterBorder: Boolean = false,
    hasInnerBorder: Boolean = false,
): Modifier {
    return if (hasOuterBorder || hasInnerBorder) {
        val innerBorderSize = avatarSize.resolveInnerBorderSizeFromAvatarSize()
        val outerBorderSize = totalBorderSize - innerBorderSize
        this
            .size(avatarSize + totalBorderSize * 2)
            .border(
                width = outerBorderSize,
                brush = borderBrush,
                shape = CircleShape,
            )
            .padding(outerBorderSize)
            .then(
                if (hasInnerBorder) {
                    Modifier
                        .border(
                            width = innerBorderSize,
                            color = AppTheme.colorScheme.background,
                            shape = CircleShape,
                        )
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
    } else {
        this
            .size(avatarSize + totalBorderSize * 2)
            .border(
                width = totalBorderSize,
                brush = transparentBorderBrush(),
                shape = CircleShape,
            )
            .padding(totalBorderSize)
            .clip(CircleShape)
    }
}

@Suppress("MagicNumber")
private fun Dp.resolveOuterBorderSizeFromAvatarSize(): Dp =
    when {
        this >= 112.dp -> 4.dp
        this >= 80.dp -> 3.dp
        this >= 32.dp -> 2.dp
        this >= 24.dp -> (1.5).dp
        else -> 1.dp
    }

@Suppress("MagicNumber")
private fun Dp.resolveInnerBorderSizeFromAvatarSize(): Dp =
    when {
        this >= 100.dp -> (1.5).dp
        this >= 40.dp -> 1.dp
        this >= 32.dp -> (0.5).dp
        else -> 0.dp
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
