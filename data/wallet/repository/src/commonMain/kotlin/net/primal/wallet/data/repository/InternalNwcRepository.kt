package net.primal.wallet.data.repository

import kotlin.time.Clock
import kotlin.time.Duration
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.wallet.data.local.db.WalletDatabase

internal class InternalNwcRepository(
    private val dispatcherProvider: DispatcherProvider,
    private val database: WalletDatabase,
) {
    fun observePendingNwcEvents(userId: String) =
        database.nwcPendingEvents()
            .observeAllByUserId(userId = userId)
            .distinctUntilChanged()

    suspend fun deletePendingNwcEvents(pendingEventIds: List<String>) =
        withContext(dispatcherProvider.io()) {
            database.nwcPendingEvents().deleteByIds(eventIds = pendingEventIds)
        }

    suspend fun cleanupStalePendingNwcEvents(staleThreshold: Duration) =
        withContext(dispatcherProvider.io()) {
            val minCreatedAt = Clock.System.now().epochSeconds - staleThreshold.inWholeSeconds
            database.nwcPendingEvents().deleteEventsOlderThan(minCreatedAt = minCreatedAt)
        }
}
