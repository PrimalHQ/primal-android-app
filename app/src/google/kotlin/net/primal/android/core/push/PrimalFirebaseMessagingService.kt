package net.primal.android.core.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.primal.android.R
import timber.log.Timber

@AndroidEntryPoint
class PrimalFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenUpdater: PushNotificationsTokenUpdater

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GlobalScope.launch { tokenUpdater.updateTokenForAllUsers() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("Message data: ${message.data}")
        message.notification?.let {
            showNotification(
                title = it.title ?: "New Notification",
                messageBody = it.body ?: "",
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, messageBody: String) {
        val channelId = "primal_notifications"
        val notificationId = 0

        // Create a notification channel for Android O and above.
        val channel = NotificationChannel(
            channelId,
            "Primal Notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }
}
