package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrimalFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenUpdater: PushNotificationsTokenUpdater

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GlobalScope.launch { tokenUpdater.updateTokenForAllUsers() }
    }
}
