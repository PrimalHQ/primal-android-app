package net.primal.android.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
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

    init {
        val defaultChannelId = "all_app_events"
        val defaultChannelName = "Primal App Events"
        val channel = NotificationChannel(
            defaultChannelId,
            defaultChannelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GlobalScope.launch { tokenUpdater.updateTokenForAllUsers() }
    }
}
