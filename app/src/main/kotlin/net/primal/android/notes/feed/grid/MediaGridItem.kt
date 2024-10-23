package net.primal.android.notes.feed.grid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.InfiniteLottieAnimation
import net.primal.android.core.compose.attachment.model.isMediaAttachment
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.MediaGalleryFilled
import net.primal.android.core.compose.icons.primaliconpack.MediaVideoFilled
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.theme.AppTheme

@Composable
fun MediaGridItem(
    modifier: Modifier = Modifier,
    item: FeedPostUi,
    maxWidthPx: Int,
) {
    val firstAttachment = item.attachments.firstOrNull()
    val hasMultipleMediaAttachments = item.attachments.filter { it.isMediaAttachment() }.size > 1
    val cdnResource = firstAttachment?.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square_dark
        false -> R.raw.primal_loader_generic_square_light
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd,
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(cdnResource?.mediaUrl ?: firstAttachment?.thumbnailUrl ?: firstAttachment?.url)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                InfiniteLottieAnimation(resId = animationRawResId)
            },
        )
        when {
            hasMultipleMediaAttachments -> {
                Icon(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 4.dp)
                        .shadow(elevation = 4.dp, shape = AppTheme.shapes.extraSmall),
                    imageVector = PrimalIcons.MediaGalleryFilled,
                    tint = Color.Unspecified,
                    contentDescription = null,
                )
            }

            firstAttachment?.type == NoteAttachmentType.Video -> {
                Icon(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 4.dp)
                        .shadow(elevation = 4.dp, shape = AppTheme.shapes.extraSmall),
                    imageVector = PrimalIcons.MediaVideoFilled,
                    tint = Color.Unspecified,
                    contentDescription = null,
                )
            }
        }
    }
}
