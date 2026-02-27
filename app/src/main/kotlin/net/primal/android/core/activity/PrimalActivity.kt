package net.primal.android.core.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.navigation.splash.SplashViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.defaultPrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.ContentDisplaySettings

@AndroidEntryPoint
abstract class PrimalActivity : FragmentActivity() {
    @Inject
    lateinit var themeStore: ActiveThemeStore

    @Inject
    lateinit var activeAccountStore: ActiveAccountStore

    private val splashViewModel: SplashViewModel by viewModels()

    private lateinit var primalTheme: PrimalTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { !splashViewModel.isAuthCheckComplete.value }
        }

        super.onCreate(savedInstanceState)

        observeThemeChanges()
        primalTheme = savedInstanceState.restoreOrDefaultPrimalTheme()
    }

    @Suppress("SpreadOperator")
    @Composable
    protected fun ConfigureActivity(content: @Composable (isLoggedIn: Boolean) -> Unit) {
        val userTheme = themeStore.userThemeState.collectAsState()
        primalTheme = userTheme.value ?: defaultPrimalTheme()

        val contentDisplaySettings = produceState(initialValue = ContentDisplaySettings()) {
            activeAccountStore.activeUserAccount
                .map { it.contentDisplaySettings }
                .collect { value = it }
        }

        val primalRippleConfiguration = RippleConfiguration(
            color = AppTheme.colorScheme.outline,
            rippleAlpha = RippleDefaults.RippleAlpha,
        )

        PrimalTheme(primalTheme = primalTheme) {
            CompositionLocalProvider(
                LocalPrimalTheme provides primalTheme,
                LocalRippleConfiguration provides primalRippleConfiguration,
                LocalContentDisplaySettings provides contentDisplaySettings.value,
            ) {
                ApplyEdgeToEdge()
                val isLoggedIn = splashViewModel.isLoggedIn.collectAsState()

                content(isLoggedIn.value)
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
        val backgroundDrawable = theme.colorScheme.background.toArgb().toDrawable()
        window.setBackgroundDrawable(backgroundDrawable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("primalTheme", primalTheme.themeName)
        super.onSaveInstanceState(outState)
    }

    private fun Bundle?.restoreOrDefaultPrimalTheme(): PrimalTheme {
        return when (this) {
            null -> PrimalTheme.Midnight
            else -> {
                val themeName = this.getString("primalTheme", PrimalTheme.Midnight.themeName)
                PrimalTheme.valueOf(themeName = themeName) ?: PrimalTheme.Midnight
            }
        }
    }
}

val LocalPrimalTheme = compositionLocalOf<PrimalTheme> { error("No PrimalTheme provided.") }

val LocalContentDisplaySettings = compositionLocalOf<ContentDisplaySettings> {
    error("No ContentDisplay settings provided.")
}
