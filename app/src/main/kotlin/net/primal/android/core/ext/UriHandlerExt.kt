package net.primal.android.core.ext

import android.content.ActivityNotFoundException
import androidx.compose.ui.platform.UriHandler

fun UriHandler.openUriSafely(uri: String) {
    try {
        openUri(uri)
    } catch (_: ActivityNotFoundException) {
        runCatching {
            val scheme = if (uri.contains("@")) "mailto" else "https"
            openUri("$scheme://$uri")
        }
    } catch (_: IllegalArgumentException) {
        runCatching {
            openUri("https://$uri")
        }
    }
}
