package net.primal.android.notes.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.notes.feed.model.FeedPostUi

@Composable
fun MediaGridItem(
    modifier: Modifier = Modifier,
    item: FeedPostUi,
    maxWidthPx: Int,
) {
    val attachment = item.attachments.firstOrNull()
    val cdnResource = attachment?.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    SubcomposeAsyncImage(
        modifier = modifier.fillMaxSize(),
        model = ImageRequest.Builder(LocalContext.current)
            .data(cdnResource?.mediaUrl ?: attachment?.thumbnailUrl ?: attachment?.url)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}
