package net.primal.android.wallet.nwc.handler

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.service.PrimalNwcService

@Singleton
class NwcSessionHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun isServiceRunningForUser(userId: String): Boolean = userId in PrimalNwcService.activeUserIds.value

    fun startService(userId: String) = PrimalNwcService.start(context, userId)
}
