package net.primal.android.core.service

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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.di.RemoteSignerServiceFactory
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

@AndroidEntryPoint
class PrimalRemoteSignerService : Service() {

    @Inject
    lateinit var remoteSignerServiceFactory: RemoteSignerServiceFactory

    private var signer: RemoteSignerService? = null

    companion object {
        private const val CHANNEL_ID = "remote_signer"
        private const val NOTIF_ID = 42

        /* TODO(marko): actually handle action stop. */
        private const val ACTION_STOP = "net.primal.signer.STOP"

        fun start(context: Context) {
            val i = Intent(context, PrimalRemoteSignerService::class.java)
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, PrimalRemoteSignerService::class.java).apply { action = ACTION_STOP }
            context.startService(i)
        }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    inner class RemoteSignerBinder : Binder() {
        val service: PrimalRemoteSignerService get() = this@PrimalRemoteSignerService
    }

    private val binder = RemoteSignerBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, RemoteSignerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.primal_wave_logo_summer)
            .setContentTitle("Primal Remote Signer")
            .setContentText("Listening for signing requests...")
            .setOngoing(true)
            .addAction(0, "Stop", stopPi)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            notification,
            if (Build.VERSION.SDK_INT >= 34)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0,
        )

        scope.launch {
            val keypair = NostrKeyPair(
                pubKey = "npub1ng4zmxu622hgw0uv35tq28gxaycph58vfnu0a5gdlhkwxaeyaddq4mx5sf",
                privateKey = "nsec1mtydyr59yfsudd8kstkue4dakvcqdjm6jmkvvwmp08cfjm7e9r2sucawh7",
            )

            signer = remoteSignerServiceFactory.create(keypair)
            signer?.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        job.cancel()
        signer?.stop()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Remote Signer",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }
}
