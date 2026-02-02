package net.primal.android.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.primal.android.R
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.di.NwcServiceFactory
import net.primal.domain.connections.nostr.NwcService

@AndroidEntryPoint
class PrimalNwcService : Service() {

    var nwcService: NwcService? = null

    @Inject
    lateinit var nwcServiceFactory: NwcServiceFactory

    @Inject
    lateinit var activeAccountStore: ActiveAccountStore

    companion object {
        const val CHANNEL_ID = "nwc_wallet_service"
        private const val NOTIFICATION_ID = 100

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, PrimalNwcService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PrimalNwcService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        nwcService = nwcServiceFactory.create()
        val notification = buildNotification()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )

        val userId = activeAccountStore.activeUserId.value
        if (userId.isNotBlank()) {
            nwcService?.initialize(userId)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        nwcService?.destroy()
        nwcService = null
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.nwc_service_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle(getString(R.string.nwc_service_notification_title))
            .setContentText(getString(R.string.nwc_service_notification_text))
            .setOngoing(true)
            .build()
}
