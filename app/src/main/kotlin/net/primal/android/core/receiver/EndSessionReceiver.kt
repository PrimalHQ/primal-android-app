package net.primal.android.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler

const val RECEIVER_SESSION_ID = "sessionId"

@AndroidEntryPoint
class EndSessionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var sessionHandler: RemoteSignerSessionHandler

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(RECEIVER_SESSION_ID) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            sessionHandler.endSession(sessionId)
        }
    }
}
