package net.primal.android

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.primal.android.navigation.PrimalAppNavigation
import net.primal.android.theme.PrimalRippleTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.defaultPrimalTheme
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
            val defaultTheme = defaultPrimalTheme()
            val primalTheme = userTheme.value ?: defaultTheme

            LaunchedEffect(userTheme.value) {
                if (userTheme.value == null) {
                    lifecycleScope.launch {
                        themeStore.setUserTheme(theme = defaultTheme.themeName)
                    }
                }
            }

            PrimalTheme(
                primalTheme = primalTheme
            ) {
                CompositionLocalProvider(
                    LocalPrimalTheme provides primalTheme,
                    LocalRippleTheme provides PrimalRippleTheme,
                ) {
                    PrimalAppNavigation()
                }
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

val LocalPrimalTheme = compositionLocalOf<PrimalTheme> { error("No PrimalTheme Provided") }
