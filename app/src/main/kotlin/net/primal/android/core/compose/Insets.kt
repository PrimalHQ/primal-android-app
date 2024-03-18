package net.primal.android.core.compose

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import net.primal.android.LocalPrimalTheme

@Composable
fun ApplyEdgeToEdge(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    isDarkTheme: Boolean = LocalPrimalTheme.current.isDarkTheme,
) {
    val context = LocalContext.current
    if (context is ComponentActivity) {
        context.applyEdgeToEdge(
            statusBarColor = statusBarColor,
            navigationBarColor = navigationBarColor,
            isDarkTheme = isDarkTheme,
        )
    }
}

fun ComponentActivity.applyEdgeToEdge(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    isDarkTheme: Boolean,
) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = statusBarColor.toArgb(),
            darkScrim = statusBarColor.toArgb(),
            detectDarkMode = { isDarkTheme },
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = navigationBarColor.toArgb(),
            darkScrim = navigationBarColor.toArgb(),
            detectDarkMode = { isDarkTheme },
        ),
    )
}
