package net.primal.android

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.primal.android.core.activity.PrimalActivity
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.main.REQUESTED_TAB_KEY
import net.primal.android.navigation.PrimalAppNavigation
import net.primal.android.navigation.asUrlDecoded
import net.primal.android.navigation.navigateToNostrConnectBottomSheet
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.NostrNotary.NotarySideEffect
import net.primal.android.nostrconnect.utils.isNostrConnectUrl
import net.primal.android.scanner.analysis.QrCodeResultDecoder
import net.primal.android.signer.client.launchSignEvent
import net.primal.android.signer.client.rememberAmberSignerLauncher

@AndroidEntryPoint
class MainActivity : PrimalActivity() {
    @Inject
    lateinit var nostrNotary: NostrNotary

    @Inject
    lateinit var qrCodeResultDecoder: QrCodeResultDecoder

    private val deepLinkIntents = MutableSharedFlow<Intent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Emit cold-start intent for tab/connect deep links (Navigation handles everything else)
        if (intent?.data != null && isSpecialMainScreenDeepLink(intent)) {
            deepLinkIntents.tryEmit(intent)
        }

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

            val navController = rememberNavController()

            LaunchedEffect(navController) {
                deepLinkIntents.collect { intent ->
                    handleSpecialDeepLinks(navController, intent)
                }
            }

            ConfigureActivity { isLoggedIn ->
                CompositionLocalProvider(LocalQrCodeDecoder provides qrCodeResultDecoder) {
                    PrimalAppNavigation(
                        navController = navController,
                        startDestination = if (isLoggedIn) "main" else "welcome",
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkIntents.tryEmit(intent)
    }

    private fun handleSpecialDeepLinks(navController: NavController, intent: Intent) {
        val uri = intent.data ?: return
        val uriString = uri.toString()
        when {
            uri.host == "live" -> Unit
            uriString.isNostrConnectUrl() -> {
                navController.navigateToNostrConnectBottomSheet(url = uriString.asUrlDecoded() ?: uriString)
            }
            else -> {
                val tab = resolveTabFromDeepLink(uri)
                if (tab != null) {
                    setMainScreenRequestedTab(navController, tab)
                } else {
                    navController.handleDeepLink(intent)
                }
            }
        }
    }

    private fun setMainScreenRequestedTab(navController: NavController, tab: PrimalTopLevelDestination) {
        runCatching { navController.getBackStackEntry("main") }
            .getOrNull()
            ?.savedStateHandle
            ?.set(REQUESTED_TAB_KEY, tab.name)
    }

    private fun isSpecialMainScreenDeepLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        val uriString = uri.toString()
        return uriString.isNostrConnectUrl() || resolveTabFromDeepLink(uri) != null
    }

    private fun resolveTabFromDeepLink(uri: Uri): PrimalTopLevelDestination? {
        return when (uri.path) {
            "/reads" -> PrimalTopLevelDestination.Reads
            "/explore" -> PrimalTopLevelDestination.Explore
            "/notifications" -> PrimalTopLevelDestination.Alerts
            else -> null
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            finishAndRemoveTask()
        }

        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }
}

val LocalQrCodeDecoder = compositionLocalOf<QrCodeResultDecoder> { error("No QrCodeResultDecoder provided.") }
