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
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
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
import net.primal.android.core.receiver.EndSessionReceiver
import net.primal.android.core.receiver.RECEIVER_SESSION_ID
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.model.RemoteAppSession
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.utils.asEllipsizedNpub

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

    @Inject
    lateinit var accountsStore: UserAccountsStore

    private var signer: RemoteSignerService? = null

    private var shownSessionIds = emptySet<String>()

    companion object {
        private const val GROUP_ID = "net.primal.CONNECTED_APPS"
        private const val END_SESSION_ACTION_INTENT = "net.primal.END_SESSION"
        private const val SUMMARY_NOTIFICATION_ID = 42
        private const val CHILD_NOTIFICATION_ID = 43
        private const val RESPOND_NOTIFICATION_ID = 44

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        fun ensureServiceStarted(context: Context) {
            if (!isServiceRunning.value) {
                start(context = context)
            }
        }

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

    private fun hideActiveAppNotification(sessionId: String) {
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
        NotificationCompat.Builder(this, PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_winter)
            .setContentTitle(resources.getString(R.string.signer_notification_summary_title))
            .setOngoing(true)
            .setGroup(GROUP_ID)
            .setGroupSummary(true)
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

        return NotificationCompat.Builder(this, PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_winter)
            .setContentTitle(
                resources.getQuantityString(
                    R.plurals.signer_notification_new_signer_request,
                    eventsCount,
                    eventsCount,
                ),
            )
            .setContentIntent(contentPendingIntent)
            .build()
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
        hideRespondNotification()
        signer?.destroy()
        scope.cancel()
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
                        sessionMap[sessionId]?.let {
                            showActiveAppNotification(it)
                        }
                    }

                    endedSessions.forEach { sessionId ->
                        hideActiveAppNotification(sessionId = sessionId)
                    }

                    shownSessionIds = sessionIds
                }
        }

    private fun observeSessionEventsPendingUserAction() =
        scope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            sessionEventRepository.observeEventsPendingUserActionForRemoteSigner(signerPubKey = signerKeyPair.pubKey)
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
                PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID,
                resources.getString(R.string.signer_notification_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    suspend fun loadBitmapFromUrl(url: String): Bitmap? {
        val loader = ImageLoader(this)

        val requestBuilder = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false)

        requestBuilder.transformations(CircleCropTransformation())

        val drawable = loader.execute(requestBuilder.build()).drawable ?: return null
        return (drawable as? BitmapDrawable)?.bitmap
    }

    @SuppressLint("MissingPermission")
    suspend fun showActiveAppNotification(session: RemoteAppSession) {
        val appIconBitmap = session.image?.let { loadBitmapFromUrl(it) }
        val displayName = accountsStore
            .findByIdOrNull(session.userPubKey)
            ?.authorNameUiFriendly()
            ?: session.userPubKey.asEllipsizedNpub()

        val notification = NotificationCompat.Builder(this, PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.primal_wave_logo_winter)
            .run {
                if (appIconBitmap != null) {
                    this.setLargeIcon(appIconBitmap)
                } else {
                    this.setLargeIcon(Icon.createWithResource(baseContext, R.drawable.remote_login))
                }
            }
            .setContentTitle(session.name)
            .setContentText(resources.getString(R.string.signer_notification_logged_in_as, displayName))
            .setGroup(GROUP_ID)
            .setContentIntent(deepLinkPendingIntent(clientPubKey = session.clientPubKey))
            .setContentTitle(session.name ?: resources.getString(R.string.signer_notification_unknown_app))
            .addAction(
                NotificationCompat.Action(
                    null,
                    resources.getString(R.string.signer_notification_end_session),
                    endSessionPendingIntent(sessionId = session.sessionId),

                ),
            )
            .build()

        NotificationManagerCompat.from(this)
            .notify(session.sessionId, CHILD_NOTIFICATION_ID, notification)
    }

    private fun deepLinkPendingIntent(clientPubKey: String): PendingIntent {
        val uri = "primal://signer/$clientPubKey".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri, this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
        }
        return PendingIntent.getActivity(
            this,
            clientPubKey.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun endSessionPendingIntent(sessionId: String): PendingIntent {
        val intent = Intent(this, EndSessionReceiver::class.java).apply {
            action = END_SESSION_ACTION_INTENT
            putExtra(RECEIVER_SESSION_ID, sessionId)
        }

        return PendingIntent.getBroadcast(
            this,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
