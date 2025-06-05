package net.primal.android.explore.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import net.primal.android.events.ui.findNearestOrNull
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun FollowPackCoverImage(
    height: Dp,
    coverImage: CdnImage?,
    modifier: Modifier = Modifier,
    clipShape: Shape? = null,
) {
    val imageLoader = LocalContext.current.imageLoader
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val boxMaxWidth = this.maxWidth

        val variant = coverImage?.variants?.findNearestOrNull(
            maxWidthPx = with(LocalDensity.current) { boxMaxWidth.roundToPx() },
        )
        val imageSource = variant?.mediaUrl ?: coverImage?.sourceUrl

        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .run {
                    if (clipShape != null) {
                        this.clip(clipShape)
                    } else {
                        this
                    }
                }
                .background(color = AppTheme.colorScheme.outline),
            model = imageSource,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}
