package net.primal.android.signer.provider

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class AppDisplayInfo(
    val name: String,
    val icon: Drawable? = null,
)

@Composable
fun rememberAppDisplayInfo(packageName: String, fallbackAppName: String? = null): AppDisplayInfo {
    val context = LocalContext.current
    return remember(packageName, fallbackAppName) {
        val appInfo = runCatching {
            context.packageManager.getApplicationInfo(packageName, 0)
        }.getOrNull()

        if (appInfo != null) {
            AppDisplayInfo(
                name = context.packageManager.getApplicationLabel(appInfo).toString(),
                icon = context.packageManager.getApplicationIcon(appInfo),
            )
        } else {
            AppDisplayInfo(
                name = fallbackAppName ?: packageName,
                icon = null,
            )
        }
    }
}
