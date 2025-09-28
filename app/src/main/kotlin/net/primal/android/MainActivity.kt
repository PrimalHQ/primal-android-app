package net.primal.android

import android.content.ActivityNotFoundException
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.navigation.PrimalAppNavigation
import net.primal.android.navigation.splash.SplashViewModel
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.NostrNotary.NotarySideEffect
import net.primal.android.scanner.analysis.QrCodeResultDecoder
import net.primal.android.signer.launchSignEvent
import net.primal.android.signer.rememberAmberSignerLauncher
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

    @Inject
    lateinit var nostrNotary: NostrNotary

    @Inject
    lateinit var qrCodeResultDecoder: QrCodeResultDecoder

    private lateinit var primalTheme: PrimalTheme

    private val splashViewModel: SplashViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { !splashViewModel.isAuthCheckComplete.value }
        }
        super.onCreate(savedInstanceState)
        observeThemeChanges()
        primalTheme = savedInstanceState.restoreOrDefaultPrimalTheme()

        setContent {
            val context = LocalContext.current

            val signLauncher = rememberAmberSignerLauncher(
                onFailure = { nostrNotary.onFailure() },
                onSuccess = nostrNotary::onSuccess,
            )
            val amberUnavailableMessage = stringResource(id = R.string.app_error_amber_unavailable)
            LaunchedEffect(nostrNotary, nostrNotary.effects) {
                nostrNotary.effects.collect {
                    when (it) {
                        is NotarySideEffect.RequestSignature -> {
                            try {
                                signLauncher.launchSignEvent(it.unsignedEvent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    amberUnavailableMessage,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                }
            }

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
                    LocalQrCodeDecoder provides qrCodeResultDecoder,
                ) {
                    ApplyEdgeToEdge()

                    val isLoggedIn = splashViewModel.isLoggedIn.collectAsState()
                    PrimalAppNavigation(startDestination = if (isLoggedIn.value) "home" else "welcome")
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            finishAndRemoveTask()
        }

        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
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

val LocalContentDisplaySettings = compositionLocalOf<ContentDisplaySettings> {
    error("No ContentDisplay settins provided.")
}

val LocalQrCodeDecoder = compositionLocalOf<QrCodeResultDecoder> { error("No QrCodeResultDecoder provided.") }
