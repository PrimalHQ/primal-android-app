package net.primal.android.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.imageLoader
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.fade
import io.github.fornewid.placeholder.material3.placeholder
import net.primal.android.theme.AppTheme

@Composable
fun PrimalAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    error: () -> Unit = {},
) {
    var loading by remember(model) { mutableStateOf(true) }

    AsyncImage(
        model = model,
        modifier = modifier
            .placeholder(
                visible = loading,
                color = AppTheme.colorScheme.surface,
                highlight = PlaceholderHighlight.fade(
                    highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
            ),
        onSuccess = { loading = false },
        onLoading = { loading = true },
        onError = {
            loading = false
            error()
        },
        contentDescription = null,
        alignment = alignment,
        contentScale = contentScale,
        imageLoader = imageLoader,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

//@Composable
//fun PrimalAsyncImage(
//    model: Any?,
//    modifier: Modifier = Modifier,
//    imageLoader: ImageLoader = LocalContext.current.imageLoader,
//    alignment: Alignment = Alignment.Center,
//    contentScale: ContentScale = ContentScale.Fit,
//    alpha: Float = DefaultAlpha,
//    colorFilter: ColorFilter? = null,
//    error: @Composable () -> Unit = {},
//    loading: @Composable () -> Unit = {},
//) {
//    val painter = rememberAsyncImagePainter(model = model, imageLoader = imageLoader)
//    val state by painter.state.collectAsState()
//
//    Box(contentAlignment = Alignment.Center) {
//        Image(
//            painter = painter,
//            modifier = modifier,
//            contentDescription = null,
//            alignment = alignment,
//            contentScale = contentScale,
//            alpha = alpha,
//            colorFilter = colorFilter,
//        )
//
//        when (state) {
//            AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading -> loading()
//            is AsyncImagePainter.State.Error -> error()
//            is AsyncImagePainter.State.Success -> Unit
//        }
//    }
//}
