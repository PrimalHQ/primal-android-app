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
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.first
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
import net.primal.android.signer.client.rememberSignerSignLauncher
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.domain.profile.Nip05VerificationService
import net.primal.domain.profile.Nip05VerificationStatus

@AndroidEntryPoint
class MainActivity : PrimalActivity() {

    override val prefetchFeedsOnSplash: Boolean = true

    @Inject
    lateinit var nostrNotary: NostrNotary

    @Inject
    lateinit var qrCodeResultDecoder: QrCodeResultDecoder

    @Inject
    lateinit var nip05VerificationService: Nip05VerificationService

    private val deepLinkIntents = MutableSharedFlow<Intent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.optOutOfNavigationTaskRestart()

        // Emit cold-start intent for tab/connect deep links (Navigation handles everything else).
        // Only on a fresh launch: the Activity keeps its launch intent across re-creations, so re-emitting
        // it after a configuration change would re-open the connect sheet or snap back to the linked tab.
        if (savedInstanceState == null && intent?.data != null && isSpecialMainScreenDeepLink(intent)) {
            deepLinkIntents.tryEmit(intent)
        }

        setContent {
            val context = LocalContext.current

            val signLauncher = rememberSignerSignLauncher(
                onFailure = { nostrNotary.onFailure() },
                onSuccess = nostrNotary::onSuccess,
            )
            val signerUnavailableMessage = stringResource(id = R.string.app_error_signer_unavailable)
            LaunchedEffect(nostrNotary, nostrNotary.effects) {
                nostrNotary.effects.collect {
                    when (it) {
                        is NotarySideEffect.RequestSignature -> {
                            try {
                                signLauncher.launchSignEvent(
                                    event = it.unsignedEvent,
                                    signerPackageName = it.signerPackageName,
                                )
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    signerUnavailableMessage,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                }
            }

            val navController = rememberNavController()

            LaunchedEffect(navController) {
                // Suspends until NavHost sets the graph; deep links can not resolve against an empty back stack.
                navController.currentBackStackEntryFlow.first()
                deepLinkIntents.collect { intent ->
                    handleSpecialDeepLinks(navController, intent)
                }
            }

            ConfigureActivity { isLoggedIn ->
                CompositionLocalProvider(
                    LocalQrCodeDecoder provides qrCodeResultDecoder,
                    LocalNip05VerificationService provides nip05VerificationService,
                ) {
                    // Building the nav graph before the auth check completes would pick a start destination
                    // that then changes, rebuilding the graph and discarding any deep link destination.
                    val isAuthCheckComplete = splashViewModel.isAuthCheckComplete.collectAsState()
                    if (isAuthCheckComplete.value) {
                        PrimalAppNavigation(
                            navController = navController,
                            startDestination = if (isLoggedIn) "main" else "welcome",
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.optOutOfNavigationTaskRestart()
        deepLinkIntents.tryEmit(intent)
    }

    /**
     * androidx Navigation restarts the whole task when it handles a deep link that arrives with
     * [Intent.FLAG_ACTIVITY_NEW_TASK] and without [Intent.FLAG_ACTIVITY_CLEAR_TASK]: it re-launches
     * through a TaskStackBuilder and finishes this Activity, so onCreate runs twice per deep link.
     * Pre-setting the flag opts out of that restart. Navigation still treats it as a new task and
     * rebuilds the back stack from the graph root.
     */
    private fun Intent.optOutOfNavigationTaskRestart() {
        if (data != null) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
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

val LocalNip05VerificationService = compositionLocalOf<Nip05VerificationService> {
    object : Nip05VerificationService {
        override suspend fun getStatus(pubkey: String) = null
        override suspend fun getStatuses(pubkeys: List<String>) = emptyMap<String, Nip05VerificationStatus?>()
        override fun observeStatus(pubkey: String) = kotlinx.coroutines.flow.emptyFlow<Nip05VerificationStatus?>()
        override suspend fun verifyIfNeeded(pubkey: String, internetIdentifier: String) = Unit
        override suspend fun verifyEagerly(pubkey: String, internetIdentifier: String) = Unit
    }
}
