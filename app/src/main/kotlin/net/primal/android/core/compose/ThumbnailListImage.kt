package net.primal.android.core.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.theme.AppTheme

@Composable
fun AvatarThumbnailListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderGradientColors: List<Color> = listOf(
        AppTheme.extraColorScheme.brand1,
        AppTheme.extraColorScheme.brand2,
    ),
) {
    SubcomposeAsyncImage(
        model = source,
        modifier = modifier.adjustAvatarBackground(
            size = size,
            hasBorder = hasBorder,
            borderGradientColors = borderGradientColors,
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer()),
            )
        },
        error = { DefaultAvatarThumbnailPlaceholderListItemImage() },
    )
}

private fun Modifier.adjustAvatarBackground(
    size: Dp = 48.dp,
    hasBorder: Boolean = false,
    borderGradientColors: List<Color>,
): Modifier = composed {
    if (hasBorder) {
        this.size(size + 2.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(borderGradientColors),
                shape = CircleShape
            )
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = AppTheme.colorScheme.primary,
                spotColor = AppTheme.colorScheme.primary,
            )
            .clip(CircleShape)
    } else {
        this.size(size)
            .clip(CircleShape)
    }

}

@Composable
fun DefaultAvatarThumbnailPlaceholderListItemImage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PrimalIcons.AvatarDefault,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
