package net.primal.android.core.compose

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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.material.placeholder
import net.primal.android.theme.AppTheme

@Composable
fun PostImageListItemImage(
    source: Any?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = source,
        modifier = modifier,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = { PostImageLoadingPlaceholder() },
        error = { PostImageErrorImage() },
    )
}

@Composable
fun PostImageLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .placeholder(
                visible = true,
                color = AppTheme.colorScheme.surface,
                highlight = PlaceholderHighlight.fade(
                    highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt,
                )
            ),
    )
}

@Composable
fun PostImageErrorImage() {
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
