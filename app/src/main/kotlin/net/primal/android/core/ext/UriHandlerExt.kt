package net.primal.android.core.ext

import android.content.ActivityNotFoundException
import androidx.compose.ui.platform.UriHandler
import timber.log.Timber

fun UriHandler.openUriSafely(uri: String) {
    try {
        openUri(uri)
    } catch (error: ActivityNotFoundException) {
        Timber.w(error)
        runCatching {
            val scheme = if (uri.contains("@")) "mailto" else "https"
            openUri("$scheme://$uri")
        }
    }
}
