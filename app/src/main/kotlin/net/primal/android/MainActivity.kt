package net.primal.android

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.primal.android.navigation.PrimalAppNavigation
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.active.ActiveThemeStore
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeStore: ActiveThemeStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        observeThemeChanges()

        setContent {
            val userTheme = themeStore.userThemeState.collectAsState()

            PrimalTheme(
                theme = userTheme.value
            ) {
                PrimalAppNavigation()
            }
        }
    }

    private fun observeThemeChanges() = lifecycleScope.launch {
        themeStore.userThemeState
            .filterNotNull()
            .collect {
                applyWindowBackgroundFromTheme(it)
            }
    }

    private fun applyWindowBackgroundFromTheme(theme: PrimalTheme) {
        val backgroundDrawable = ColorDrawable(theme.colorScheme.background.toArgb())
        window.setBackgroundDrawable(backgroundDrawable)
    }
}
