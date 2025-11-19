package net.primal.android.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.primal.android.MainActivity
import net.primal.android.R
import net.primal.android.core.di.RemoteSignerServiceFactory
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService

@AndroidEntryPoint
class PrimalRemoteSignerService : Service(), DefaultLifecycleObserver {

    @Inject
    lateinit var remoteSignerServiceFactory: RemoteSignerServiceFactory

    @Inject
    lateinit var credentialsStore: CredentialsStore

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var sessionEventRepository: SessionEventRepository

    private var signer: RemoteSignerService? = null

    private var shownSessionIds = emptySet<String>()

    companion object {
        private const val GROUP_ID = "net.primal.CONNECTED_APPS"
        private const val CHANNEL_ID = "remote_signer"
        private const val SUMMARY_NOTIFICATION_ID = 42
        private const val CHILD_NOTIFICATION_ID = 43
        private const val RESPOND_NOTIFICATION_ID = 44

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val i = Intent(context, PrimalRemoteSignerService::class.java)
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, PrimalRemoteSignerService::class.java)
            context.stopService(i)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    inner class RemoteSignerBinder : Binder() {
        val service: PrimalRemoteSignerService get() = this@PrimalRemoteSignerService
    }

    private val binder = RemoteSignerBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super<Service>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        _isServiceRunning.value = true
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val summaryNotification = buildSummaryNotification()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)

        ServiceCompat.startForeground(
            this,
            CHILD_NOTIFICATION_ID,
            summaryNotification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )

        scope.launch {
            signer = remoteSignerServiceFactory.create(
                credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair(),
            )

            signer?.initialize()
        }

        observeOngoingSessions()
        observeSessionEventsPendingUserAction()

        return START_STICKY
    }

    private fun showSessionNotification(session: AppSession) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            session.sessionId,
            CHILD_NOTIFICATION_ID,
            buildChildNotification(session = session),
        )
    }

    private fun hideSessionNotification(sessionId: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(sessionId, CHILD_NOTIFICATION_ID)
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, buildSummaryNotification())
    }

    private fun showRespondNotification(eventsCount: Int) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            RESPOND_NOTIFICATION_ID,
            buildRespondNotification(eventsCount = eventsCount),
        )
    }

    private fun hideRespondNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(RESPOND_NOTIFICATION_ID)
    }

    override fun onStart(owner: LifecycleOwner) {
        hideRespondNotification()
        super<DefaultLifecycleObserver>.onStart(owner)
    }

    private fun buildSummaryNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle("Active Apps")
            .setOngoing(true)
            .setGroup(GROUP_ID)
            .setGroupSummary(true)
            .build()

    private fun buildChildNotification(session: AppSession): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.default_avatar)
            .setContentTitle(session.name ?: "Unknown App")
            .setContentText(session.sessionState.name)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_ID)
            .build()

    private fun buildRespondNotification(eventsCount: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle("You have $eventsCount new signer request(s).")
            .setContentIntent(contentPendingIntent)
            .build()
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
        hideRespondNotification()
        signer?.destroy()
        scope.cancel()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super<Service>.onDestroy()
    }

    private fun observeOngoingSessions() =
        scope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            sessionRepository.observeOngoingSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { sessions ->
                    if (sessions.isEmpty()) {
                        stopSelf()
                        return@collect
                    }

                    val sessionMap = sessions.associateBy { it.sessionId }
                    val sessionIds = sessions.map { it.sessionId }.toSet()
                    val endedSessions = shownSessionIds - sessionIds

                    sessionIds.forEach { sessionId ->
                        sessionMap[sessionId]?.let { showSessionNotification(it) }
                    }

                    endedSessions.forEach { sessionId ->
                        hideSessionNotification(sessionId = sessionId)
                    }

                    shownSessionIds = sessionIds
                }
        }

    private fun observeSessionEventsPendingUserAction() =
        scope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            sessionEventRepository.observeEventsPendingUserAction(signerPubKey = signerKeyPair.pubKey)
                .collect { events ->
                    if (events.isNotEmpty() && isAppInBackground()) {
                        showRespondNotification(eventsCount = events.size)
                    }
                }
        }

    private fun isAppInBackground() =
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .currentState
            .isAtLeast(Lifecycle.State.STARTED)
            .not()

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Remote Signer",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }
}
