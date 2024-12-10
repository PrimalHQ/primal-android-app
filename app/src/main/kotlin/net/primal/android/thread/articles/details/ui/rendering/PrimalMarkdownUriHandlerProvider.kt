package net.primal.android.thread.articles.details.ui.rendering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

@Composable
fun PrimalMarkdownUriHandlerProvider(linkClickHandler: (uri: String) -> Unit, content: @Composable () -> Unit) {
    val uriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) = linkClickHandler(uri)
        }
    }
    CompositionLocalProvider(LocalUriHandler provides uriHandler, content)
}
