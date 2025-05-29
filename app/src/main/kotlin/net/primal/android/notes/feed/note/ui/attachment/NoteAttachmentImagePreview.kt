package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.fade
import io.github.fornewid.placeholder.material3.placeholder
import me.saket.telephoto.zoomable.rememberZoomablePeekOverlayState
import me.saket.telephoto.zoomable.zoomablePeekOverlay
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.ui.findNearestOrNull
import net.primal.android.theme.AppTheme
import net.primal.core.networking.blossom.resolveBlossomUrls
import net.primal.domain.links.EventUriType

@Composable
fun NoteAttachmentImagePreview(
    attachment: EventUriUi,
    blossoms: List<String>,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val cdnImageSource = when (attachment.type) {
        EventUriType.Image -> {
            val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
            variant?.mediaUrl
        }
        else -> attachment.thumbnailUrl
    }

    val blossomUrls = resolveBlossomUrls(originalUrl = attachment.url, blossoms = blossoms)
    val imageUrls = ((listOfNotNull(cdnImageSource, attachment.url) + blossomUrls)).distinct()
    var currentUrlIndex by remember { mutableIntStateOf(0) }
    val currentUrl = imageUrls.getOrNull(currentUrlIndex)

    if (currentUrl != null) {
        SubcomposeAsyncImage(
            model = currentUrl,
            imageLoader = LocalContext.current.imageLoader,
            modifier = modifier.zoomablePeekOverlay(state = rememberZoomablePeekOverlayState()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { NoteImageLoadingPlaceholder() },
            error = { currentUrlIndex += 1 },
        )
    } else {
        NoteImageErrorImage()
    }
}

@Composable
fun NoteImageLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .placeholder(
                visible = true,
                color = AppTheme.colorScheme.surface,
                highlight = PlaceholderHighlight.fade(
                    highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
            ),
    )
}

@Composable
fun NoteImageErrorImage() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt3),
    )
}
