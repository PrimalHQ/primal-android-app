package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.fade
import io.github.fornewid.placeholder.material3.placeholder
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.domain.findNearestOrNull
import net.primal.android.theme.AppTheme
import net.primal.domain.EventUriType

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
            modifier = modifier,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { NoteImageLoadingPlaceholder() },
            error = { currentUrlIndex += 1 },
        )
    } else {
        NoteImageErrorImage(
            modifier = modifier,
        )
    }
}

private fun resolveBlossomUrls(originalUrl: String, blossoms: List<String>): List<String> {
    val fileName = originalUrl.extractFileHashNameFromUrl()
    return blossoms.map { blossomUrl ->
        "${blossomUrl.ensureEndsWithSlash()}$fileName"
    }
}

private fun String.extractFileHashNameFromUrl(): String? {
    val hashStartIndex = this.lastIndexOf('/') + 1
    return if (hashStartIndex != -1 && hashStartIndex < this.length) {
        this.substring(hashStartIndex)
    } else {
        null
    }
}

private fun String.ensureEndsWithSlash(): String {
    return if (this.endsWith("/")) this else "$this/"
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
fun NoteImageErrorImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
    }
}
