package net.primal.android.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.di.RemoteSignerServiceFactory
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService

@AndroidEntryPoint
class PrimalRemoteSignerService : Service() {

    @Inject
    lateinit var remoteSignerServiceFactory: RemoteSignerServiceFactory

    @Inject
    lateinit var credentialsStore: CredentialsStore

    @Inject
    lateinit var sessionRepository: SessionRepository

    private var signer: RemoteSignerService? = null

    private var shownSessionIds = emptySet<String>()

    companion object {
        private const val GROUP_ID = "net.primal.CONNECTED_APPS"
        private const val CHANNEL_ID = "remote_signer"
        private const val SUMMARY_NOTIFICATION_ID = 42
        private const val CHILD_NOTIFICATION_ID = 43

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
        super.onCreate()
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

            signer?.start()
        }

        observeOngoingSessions()

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

    override fun onDestroy() {
        _isServiceRunning.value = false
        signer?.stop()
        scope.cancel()
        super.onDestroy()
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
