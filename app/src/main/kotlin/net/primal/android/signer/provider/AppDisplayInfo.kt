package net.primal.android.signer.provider

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class AppDisplayInfo(
    val name: String,
    val icon: Drawable,
)

@Composable
fun rememberAppDisplayInfo(packageName: String): AppDisplayInfo? {
    val context = LocalContext.current
    return remember(packageName) {
        runCatching {
            context.packageManager.getApplicationInfo(packageName, 0)
        }.getOrNull()
            ?.let { appInfo ->
                AppDisplayInfo(
                    name = context.packageManager.getApplicationLabel(appInfo).toString(),
                    icon = context.packageManager.getApplicationIcon(appInfo),
                )
            }
    }
}
