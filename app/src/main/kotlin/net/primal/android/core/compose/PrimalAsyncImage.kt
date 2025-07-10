package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
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
    contentDescription: String? = null,
    errorColor: Color = Color.Unspecified,
    errorHighlight: PlaceholderHighlight? = null,
    placeholderColor: Color = AppTheme.colorScheme.surface,
    placeHolderHighlight: PlaceholderHighlight? = PlaceholderHighlight.fade(
        highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
    shape: Shape? = null,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    onError: () -> Unit = {},
) {
    var state by remember(model) { mutableStateOf<AsyncImagePainter.State?>(null) }

    AsyncImage(
        model = model,
        modifier = modifier
            .placeholder(
                visible = state is AsyncImagePainter.State.Error,
                color = errorColor,
                highlight = errorHighlight,
                shape = shape,
            )
            .placeholder(
                visible = state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty,
                color = placeholderColor,
                highlight = placeHolderHighlight,
                shape = shape,
            ),
        onState = {
            state = it
            if (it is AsyncImagePainter.State.Error) {
                onError()
            }
        },
        contentDescription = contentDescription,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
        alignment = alignment,
        contentScale = contentScale,
        imageLoader = imageLoader,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
fun PrimalImage(
    model: Any?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null,
    error: @Composable () -> Unit = {},
    loading: @Composable () -> Unit = {},
) {
    val painter = rememberAsyncImagePainter(model = model, imageLoader = imageLoader)
    val state by painter.state.collectAsState()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )

        when (state) {
            AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading -> loading()
            is AsyncImagePainter.State.Error -> error()
            is AsyncImagePainter.State.Success -> Unit
        }
    }
}
