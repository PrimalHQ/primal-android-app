package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PrimalFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenUpdater: PushNotificationsTokenUpdater

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag("Signer").d("Received message: $message")
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
    }
}
