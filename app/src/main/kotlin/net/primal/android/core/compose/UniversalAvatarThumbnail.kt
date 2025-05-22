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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil3.compose.SubcomposeAsyncImage
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.images.AvatarCoilImageLoader
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.theme.AppTheme
import net.primal.core.networking.blossom.resolveBlossomUrls
import net.primal.domain.links.CdnImage

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
    hasInnerBorderOverride: Boolean = true,
    isAvatarAnimated: Boolean = false,
    avatarBlossoms: List<String> = emptyList(),
    defaultAvatar: @Composable () -> Unit = { DefaultAvatarThumbnailPlaceholderListItemImage() },
) {
    val hasLegendBorder = legendaryCustomization?.avatarGlow == true &&
        legendaryCustomization.legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION

    val borderBrush = if (hasLegendBorder) {
        legendaryCustomization?.legendaryStyle?.primaryBrush
    } else {
        null
    }

    val totalBorderSize = avatarSize.resolveOuterBorderSizeFromAvatarSize() +
        avatarSize.resolveInnerBorderSizeFromAvatarSize()

    val variant = avatarCdnImage?.variants?.minByOrNull { it.width }

    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        cdnVariantUrl = variant?.mediaUrl,
        sourceUrl = avatarCdnImage?.sourceUrl,
        blossoms = avatarBlossoms,
        hasOuterBorder = hasBorder && avatarSize > 0.dp,
        hasInnerBorder = hasLegendBorder && avatarSize > 0.dp && hasInnerBorderOverride,
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
        isAvatarAnimated = isAvatarAnimated,
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
    cdnVariantUrl: String?,
    sourceUrl: String?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    blossoms: List<String> = emptyList(),
    totalBorderSize: Dp = 2.dp,
    hasOuterBorder: Boolean = false,
    hasInnerBorder: Boolean = false,
    borderBrush: Brush = defaultBorderBrush(),
    backgroundColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: (() -> Unit)? = null,
    defaultAvatar: @Composable () -> Unit,
    isAvatarAnimated: Boolean = false,
) {
    val context = LocalContext.current

    val sharedModifier = modifier
        .adjustAvatarBackground(
            avatarSize = avatarSize,
            totalBorderSize = totalBorderSize,
            borderBrush = borderBrush,
            hasOuterBorder = hasOuterBorder,
            hasInnerBorder = hasInnerBorder,
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )

    val blossomUrls = resolveBlossomUrls(originalUrl = sourceUrl, blossoms = blossoms)
    val imageUrls = ((listOfNotNull(cdnVariantUrl, sourceUrl) + blossomUrls)).distinct()
    var currentUrlIndex by remember { mutableIntStateOf(0) }
    val currentUrl = imageUrls.getOrNull(currentUrlIndex)

    val animatedContent = LocalContentDisplaySettings.current.showAnimatedAvatars
    val imageLoader = if (animatedContent || isAvatarAnimated) {
        AvatarCoilImageLoader.provideImageLoader(context = context)
    } else {
        AvatarCoilImageLoader.provideNoGifsImageLoader(context = context)
    }

    if (currentUrl != null) {
        SubcomposeAsyncImage(
            model = currentUrl,
            imageLoader = imageLoader,
            modifier = sharedModifier,
            contentDescription = stringResource(id = R.string.accessibility_profile_image),
            contentScale = ContentScale.Crop,
            loading = { AvatarLoadingBox(backgroundColor) },
            error = { currentUrlIndex += 1 },
        )
    } else {
        SubcomposeAsyncImage(
            modifier = sharedModifier,
            model = sourceUrl,
            imageLoader = imageLoader,
            contentDescription = stringResource(id = R.string.accessibility_profile_image),
            contentScale = ContentScale.Crop,
            loading = { AvatarLoadingBox(backgroundColor) },
            error = { defaultAvatar() },
        )
    }
}

@Composable
private fun AvatarLoadingBox(backgroundColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
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
        this >= 121.dp -> (5.0).dp
        this in (112.dp..120.dp) -> (4.5).dp
        this in (60.dp..112.dp) -> (3.0).dp
        this in (52.dp..60.dp) -> (2.5).dp
        this in (40.dp..52.dp) -> (2.0).dp
        this in (0.dp..40.dp) -> (1.5).dp
        else -> 0.dp
    }

@Suppress("MagicNumber")
private fun Dp.resolveInnerBorderSizeFromAvatarSize(): Dp =
    when {
        this >= 120.dp -> (2.0).dp
        this in (112.dp..120.dp) -> (1.5).dp
        this in (40.dp..112.dp) -> (1.0).dp
        this in (32.dp..40.dp) -> (0.5).dp
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
