package net.primal.android.core.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.images.AvatarCoilImageLoader
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private val LETTER_ICON_BACKGROUND
    @Composable
    get() = if (isAppInDarkPrimalTheme()) {
        Color(0xFF333333)
    } else {
        Color(0xFFDDDDDD)
    }

@Composable
fun AppIconThumbnail(
    modifier: Modifier = Modifier,
    appName: String?,
    appIconUrl: String?,
    avatarSize: Dp,
) {
    if (!appIconUrl.isNullOrBlank()) {
        val context = LocalContext.current

        PrimalAsyncImage(
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape),
            imageLoader = AvatarCoilImageLoader.provideNoGifsImageLoader(context = context),
            model = appIconUrl,
            contentScale = ContentScale.Crop,
        )
    } else {
        val fallbackCharacter = appName?.firstOrNull()?.uppercase() ?: "?"
        val textMeasurer = rememberTextMeasurer()
        val fontSize = (avatarSize.value / 2).sp
        val fontColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2

        Box(
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(LETTER_ICON_BACKGROUND)
                .border(
                    width = if (avatarSize <= 24.dp) 1.dp else 2.dp,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    shape = CircleShape,
                ),
        ) {
            Canvas(Modifier.matchParentSize()) {
                val textLayout = textMeasurer.measure(
                    text = fallbackCharacter,
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                )

                val textWidth = textLayout.size.width.toFloat()
                val textHeight = textLayout.size.height.toFloat()

                val x = (size.width - textWidth) / 2f
                val y = (size.height - textHeight) / 2f

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(x, y),
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewAppIconThumbnailBig() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        AppIconThumbnail(
            appIconUrl = null,
            appName = "Test",
            avatarSize = 48.dp,
        )
    }
}

@Composable
@Preview
fun PreviewAppIconThumbnailMedium() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        AppIconThumbnail(
            appIconUrl = null,
            appName = "Test",
            avatarSize = 36.dp,
        )
    }
}

@Composable
@Preview
fun PreviewAppIconThumbnailSmall() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        AppIconThumbnail(
            appIconUrl = null,
            appName = "Test",
            avatarSize = 24.dp,
        )
    }
}

@Composable
@Preview
fun PreviewAppIconThumbnailExtraSmall() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        AppIconThumbnail(
            appIconUrl = null,
            appName = "Test",
            avatarSize = 18.dp,
        )
    }
}
