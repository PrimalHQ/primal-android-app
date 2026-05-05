package net.primal.android.core.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import net.primal.core.utils.runCatching

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

fun Context.openUriInExternalBrowser(uri: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val browserIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        intent.selector = browserIntent
        startActivity(intent)
    }
}
