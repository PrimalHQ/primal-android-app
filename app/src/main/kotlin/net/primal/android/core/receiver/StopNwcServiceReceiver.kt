package net.primal.android.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import net.primal.android.core.service.PrimalNwcService

@AndroidEntryPoint
class StopNwcServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PrimalNwcService.ACTION_STOP_USER -> {
                intent.getStringExtra(PrimalNwcService.EXTRA_USER_ID)
                    ?.let { PrimalNwcService.stop(context, it) }
            }
        }
    }
}
