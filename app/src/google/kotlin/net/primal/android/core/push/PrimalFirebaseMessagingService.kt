package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.primal.android.nostrconnect.handler.RemoteSignerRemoteMessageHandler
import net.primal.android.wallet.nwc.handler.NwcRemoteMessageHandler
import net.primal.core.utils.coroutines.DispatcherProvider

@AndroidEntryPoint
class PrimalFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var tokenUpdater: PushNotificationsTokenUpdater

    @Inject
    lateinit var signerRemoteMessageHandler: RemoteSignerRemoteMessageHandler

    @Inject
    lateinit var nwcRemoteMessageHandler: NwcRemoteMessageHandler

    private val scope by lazy { CoroutineScope(dispatcherProvider.io() + SupervisorJob()) }

    override fun onMessageReceived(message: RemoteMessage) {
        if (signerRemoteMessageHandler.isRemoteSignerMessage(message = message)) {
            scope.launch { withTimeout(15.seconds) { signerRemoteMessageHandler.process(message = message) } }
        } else if (nwcRemoteMessageHandler.isNwcMessage(message = message)) {
            scope.launch { withTimeout(15.seconds) { nwcRemoteMessageHandler.process(message = message) } }
        }

        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            withTimeout(15.seconds) { runCatching { tokenUpdater.updateTokenForAllUsers() } }
        }
        scope.launch {
            withTimeout(15.seconds) { runCatching { tokenUpdater.updateTokenForRemoteSigner() } }
        }
        scope.launch {
            withTimeout(15.seconds) { runCatching { tokenUpdater.updateTokenForNwcService() } }
        }
    }
}
