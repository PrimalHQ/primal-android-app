package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.primal.android.nostrconnect.handler.RemoteSignerRemoteMessageHandler
import net.primal.android.wallet.nwc.handler.NwcRemoteMessageHandler

@AndroidEntryPoint
class PrimalFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenUpdater: PushNotificationsTokenUpdater

    @Inject
    lateinit var signerRemoteMessageHandler: RemoteSignerRemoteMessageHandler

    @Inject
    lateinit var nwcRemoteMessageHandler: NwcRemoteMessageHandler

    val scope = CoroutineScope(SupervisorJob())

    override fun onMessageReceived(message: RemoteMessage) {
        if (signerRemoteMessageHandler.isRemoteSignerMessage(message = message)) {
            scope.launch { signerRemoteMessageHandler.process(message = message) }
        } else if (nwcRemoteMessageHandler.isNwcMessage(message = message)) {
            scope.launch { nwcRemoteMessageHandler.process(message = message) }
        }

        super.onMessageReceived(message)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GlobalScope.launch {
            runCatching { tokenUpdater.updateTokenForAllUsers() }
        }
        GlobalScope.launch {
            runCatching { tokenUpdater.updateTokenForRemoteSigner() }
        }
        GlobalScope.launch {
            runCatching { tokenUpdater.updateTokenForNwcService() }
        }
    }
}
