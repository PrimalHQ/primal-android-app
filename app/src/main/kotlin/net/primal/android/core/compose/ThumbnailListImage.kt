package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault

@Composable
fun AvatarThumbnailListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = source,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape),
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
