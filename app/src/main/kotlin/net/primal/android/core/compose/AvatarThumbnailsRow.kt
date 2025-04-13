package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun AvatarThumbnailsRow(
    modifier: Modifier = Modifier,
    avatarCdnImages: List<CdnImage?>,
    avatarLegendaryCustomizations: List<LegendaryCustomization?> = emptyList(),
    avatarOverlap: AvatarOverlap = AvatarOverlap.End,
    hasAvatarBorder: Boolean = true,
    avatarBorderSize: Dp = 2.dp,
    avatarSize: Dp = 32.dp,
    avatarSpacing: Dp = 4.dp,
    avatarOverlapPercentage: Float = 0.25f,
    avatarBorderColor: Color = Color.White,
    maxAvatarsToShow: Int? = null,
    displayAvatarOverflowIndicator: Boolean = true,
    onClick: ((Int) -> Unit)? = null,
) {
    val avatarVisibleWidth = if (avatarOverlap.isNone()) {
        avatarSize + avatarSpacing
    } else {
        avatarSize.times(1f - avatarOverlapPercentage)
    }

    BoxWithConstraints(modifier = modifier) {
        val maxAvatars = maxAvatarsToShow ?: ((maxWidth.value / avatarVisibleWidth.value).toInt() - 2)
        val avatarsCount = avatarCdnImages.size
        val avatarsToRender = avatarsCount.coerceAtMost(maxAvatars)
        val avatarsOverflowCount = avatarsCount - avatarsToRender

        if (avatarsToRender < avatarsCount && displayAvatarOverflowIndicator && avatarOverlap.isStart()) {
            AvatarOverflowIndicator(
                width = (avatarsToRender * avatarVisibleWidth.value).dp,
                avatarSize = avatarSize,
                hasAvatarBorder = hasAvatarBorder,
                avatarBorderColor = avatarBorderColor,
                avatarsOverflowCount = avatarsOverflowCount,
                avatarBorderSize = avatarBorderSize,
            )
        }

        avatarCdnImages
            .take(avatarsToRender)
            .run { if (avatarOverlap.isStart()) this.reversed() else this }
            .forEachIndexed { index, imageCdnImage ->
                val layoutIndex = if (avatarOverlap.isStart()) {
                    avatarsToRender - index - 1
                } else {
                    index
                }

                AvatarSpacer(width = (layoutIndex * avatarVisibleWidth.value).dp) {
                    UniversalAvatarThumbnail(
                        modifier = Modifier.size(avatarSize),
                        avatarCdnImage = imageCdnImage,
                        hasBorder = hasAvatarBorder,
                        legendaryCustomization = runCatching { avatarLegendaryCustomizations[layoutIndex] }.getOrNull(),
                        fallbackBorderColor = avatarBorderColor,
                        borderSizeOverride = avatarBorderSize,
                        onClick = if (onClick != null) {
                            { onClick(layoutIndex) }
                        } else {
                            null
                        },
                    )
                }
            }

        if (avatarsToRender < avatarsCount && displayAvatarOverflowIndicator && !avatarOverlap.isStart()) {
            AvatarOverflowIndicator(
                width = (avatarsToRender * avatarVisibleWidth.value).dp,
                avatarSize = avatarSize,
                hasAvatarBorder = hasAvatarBorder,
                avatarBorderColor = avatarBorderColor,
                avatarsOverflowCount = avatarsOverflowCount,
                avatarBorderSize = avatarBorderSize,
            )
        }
    }
}

@Composable
private fun AvatarOverflowIndicator(
    width: Dp,
    avatarSize: Dp,
    hasAvatarBorder: Boolean,
    avatarBorderColor: Color,
    avatarsOverflowCount: Int,
    avatarBorderSize: Dp,
) {
    AvatarSpacer(width = width) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .adjustAvatarBackground(
                    avatarSize = 48.dp,
                    hasOuterBorder = hasAvatarBorder,
                    borderBrush = Brush.linearGradient(listOf(avatarBorderColor, avatarBorderColor)),
                    totalBorderSize = avatarBorderSize,
                )
                .background(color = moreBackgroundColor)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+${avatarsOverflowCount.coerceAtMost(maximumValue = 99)}",
                fontSize = 12.sp,
                style = AppTheme.typography.bodySmall,
                color = moreForegroundColor,
            )
        }
    }
}

enum class AvatarOverlap {
    Start,
    End,
    None,
    ;

    fun isStart() = this == Start
    fun isEnd() = this == End
    fun isNone() = this == None
}

private val moreBackgroundColor = Color(0xFFC8C8C8)
private val moreForegroundColor = Color(0xFF111111)

@Composable
private fun AvatarSpacer(width: Dp, content: @Composable () -> Unit) {
    Row {
        Spacer(
            modifier = Modifier
                .height(10.dp)
                .width(width),
        )

        content()
    }
}
