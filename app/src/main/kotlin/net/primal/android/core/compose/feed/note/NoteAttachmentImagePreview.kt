package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.theme.AppTheme

@Composable
fun NoteAttachmentImagePreview(
    attachment: NoteAttachmentUi,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val imageSource = when (attachment.type) {
        NoteAttachmentType.Image -> {
            val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
            variant?.mediaUrl ?: attachment.url
        }
        else -> attachment.thumbnailUrl
    }

    SubcomposeAsyncImage(
        model = imageSource,
        modifier = modifier,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = { NoteImageLoadingPlaceholder() },
        error = {
            SubcomposeAsyncImage(
                model = attachment.url,
                modifier = modifier,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = { NoteImageLoadingPlaceholder() },
                error = {
                    NoteImageErrorImage()
                },
            )
        },
    )
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
    }
}
