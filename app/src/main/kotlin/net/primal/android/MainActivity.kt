package net.primal.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
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

        setContent {
            val userTheme = themeStore.userThemeState.collectAsState(initial = null)

            PrimalTheme(
                theme = userTheme.value
            ) {
                PrimalAppNavigation()
            }
        }
    }

}
