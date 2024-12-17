package net.primal.android

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.navigation.PrimalAppNavigation
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.defaultPrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.ContentDisplaySettings

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var themeStore: ActiveThemeStore

    @Inject
    lateinit var activeAccountStore: ActiveAccountStore

    lateinit var primalTheme: PrimalTheme

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        observeThemeChanges()
        primalTheme = savedInstanceState.restoreOrDefaultPrimalTheme()

        setContent {
            val userTheme = themeStore.userThemeState.collectAsState()
            primalTheme = userTheme.value ?: defaultPrimalTheme(currentTheme = primalTheme)

            val contentDisplaySettings = produceState(initialValue = ContentDisplaySettings()) {
                activeAccountStore.activeUserAccount
                    .map { it.contentDisplaySettings }
                    .collect { value = it }
            }

            val primalRippleConfiguration = RippleConfiguration(
                color = AppTheme.colorScheme.outline,
                rippleAlpha = RippleDefaults.RippleAlpha,
            )

            PrimalTheme(
                primalTheme = primalTheme,
            ) {
                CompositionLocalProvider(
                    LocalPrimalTheme provides primalTheme,
                    LocalRippleConfiguration provides primalRippleConfiguration,
                    LocalContentDisplaySettings provides contentDisplaySettings.value,
                ) {
                    ApplyEdgeToEdge()

                    SharedTransitionLayout {
                        PrimalAppNavigation()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("primalTheme", primalTheme.themeName)
        super.onSaveInstanceState(outState)
    }

    private fun Bundle?.restoreOrDefaultPrimalTheme(): PrimalTheme {
        return when (this) {
            null -> PrimalTheme.Sunset
            else -> {
                val themeName = this.getString("primalTheme", PrimalTheme.Sunset.themeName)
                PrimalTheme.valueOf(themeName = themeName) ?: PrimalTheme.Sunset
            }
        }
    }

    private fun observeThemeChanges() =
        lifecycleScope.launch {
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

val LocalPrimalTheme = compositionLocalOf<PrimalTheme> { error("No PrimalTheme provided.") }

val LocalContentDisplaySettings =
    compositionLocalOf<ContentDisplaySettings> { error("No ContentDisplay settins provided.") }
