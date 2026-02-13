package net.primal.android.core.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.receiver.StopNwcServiceReceiver
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.di.NwcServiceFactory
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.nostr.utils.asEllipsizedNpub

@AndroidEntryPoint
class PrimalNwcService : Service() {

    private val nwcServices: MutableMap<String, NwcService> = mutableMapOf()

    @Inject
    lateinit var nwcServiceFactory: NwcServiceFactory

    @Inject
    lateinit var activeAccountStore: ActiveAccountStore

    @Inject
    lateinit var accountsStore: UserAccountsStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandChannel = Channel<ServiceCommand>(Channel.UNLIMITED)

    companion object {
        private const val GROUP_ID = "net.primal.NWC_WALLET_SERVICE"
        private const val SUMMARY_NOTIFICATION_ID = 200
        private const val CHILD_NOTIFICATION_ID = 201

        const val ACTION_START_USER = "net.primal.NWC_START_USER"
        const val ACTION_STOP_USER = "net.primal.NWC_STOP_USER"
        const val EXTRA_USER_ID = "userId"

        private val _activeUserIds = MutableStateFlow<Set<String>>(emptySet())
        val activeUserIds = _activeUserIds.asStateFlow()

        private val runningStateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        fun isRunningForUser(userId: String): StateFlow<Boolean> =
            activeUserIds
                .map { userId in it }
                .distinctUntilChanged()
                .stateIn(
                    scope = runningStateScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = userId in activeUserIds.value,
                )

        fun start(context: Context, userId: String) {
            val intent = Intent(context, PrimalNwcService::class.java).apply {
                action = ACTION_START_USER
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context, userId: String) {
            val intent = Intent(context, PrimalNwcService::class.java).apply {
                action = ACTION_STOP_USER
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        serviceScope.launch {
            for (command in commandChannel) {
                when (command) {
                    is ServiceCommand.Start -> handleStartUser(command.userId)
                    is ServiceCommand.Stop -> handleStopUser(command.userId)
                }
            }
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START_USER -> {
                startForeground()
                startUserService(intent.getStringExtra(EXTRA_USER_ID))
            }

            ACTION_STOP_USER -> stopUserService(intent.getStringExtra(EXTRA_USER_ID))

            else -> {
                startForeground()
                startUserService(activeAccountStore.activeUserId.value)
            }
        }

        return START_NOT_STICKY
    }

    private fun startUserService(userId: String?) {
        if (userId.isNullOrBlank()) return
        commandChannel.trySend(ServiceCommand.Start(userId))
    }

    private fun stopUserService(userId: String?) {
        if (userId == null) return
        commandChannel.trySend(ServiceCommand.Stop(userId))
    }

    private fun handleStartUser(userId: String) {
        if (nwcServices.containsKey(userId)) return
        val service = nwcServiceFactory.create()
        nwcServices[userId] = service
        service.initialize(userId)
        _activeUserIds.value = nwcServices.keys.toSet()
        updateNotifications()
    }

    private fun handleStopUser(userId: String) {
        nwcServices.remove(userId)?.destroy()
        _activeUserIds.value = nwcServices.keys.toSet()
        cancelChildNotification(userId)
        if (nwcServices.isEmpty()) {
            stopSelf()
        } else {
            updateNotifications()
        }
    }

    private fun startForeground() {
        val summaryNotification = buildSummaryNotification()
        ServiceCompat.startForeground(
            this,
            SUMMARY_NOTIFICATION_ID,
            summaryNotification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
    }

    @SuppressLint("MissingPermission")
    private fun updateNotifications() {
        val notificationManager = NotificationManagerCompat.from(this)

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, buildSummaryNotification())

        nwcServices.keys.forEach { userId ->
            val displayName = accountsStore.findByIdOrNull(userId)?.authorNameUiFriendly()
                ?: userId.asEllipsizedNpub()
            notificationManager.notify(userId, CHILD_NOTIFICATION_ID, buildChildNotification(userId, displayName))
        }
    }

    private fun cancelChildNotification(userId: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(userId, CHILD_NOTIFICATION_ID)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        nwcServices.values.forEach { it.destroy() }
        nwcServices.clear()
        _activeUserIds.value = emptySet()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
        notificationManager.cancel(CHILD_NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID,
                getString(R.string.signer_notification_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private fun buildSummaryNotification(): Notification {
        return NotificationCompat.Builder(this, PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle(getString(R.string.nwc_service_notification_title))
            .setOngoing(true)
            .setGroup(GROUP_ID)
            .setGroupSummary(true)
            .build()
    }

    private fun buildChildNotification(userId: String, displayName: String): Notification {
        val stopIntent = Intent(this, StopNwcServiceReceiver::class.java).apply {
            action = ACTION_STOP_USER
            putExtra(EXTRA_USER_ID, userId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            userId.hashCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle(getString(R.string.nwc_service_notification_user_title))
            .setContentText(getString(R.string.nwc_service_notification_text, displayName))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_ID)
            .addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.nwc_service_notification_stop),
                    stopPendingIntent,
                ),
            )
            .build()
    }

    private sealed interface ServiceCommand {
        data class Start(val userId: String) : ServiceCommand
        data class Stop(val userId: String) : ServiceCommand
    }
}
