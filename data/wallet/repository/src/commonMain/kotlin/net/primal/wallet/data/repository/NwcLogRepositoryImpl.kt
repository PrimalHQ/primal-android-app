package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.wallet.nwc.NwcLogRepository
import net.primal.domain.wallet.nwc.model.NwcRequestLog
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.nwc.mapper.asDomain

class NwcLogRepositoryImpl(
    private val walletDatabase: WalletDatabase,
    private val dispatchers: DispatcherProvider,
) : NwcLogRepository {
    override suspend fun getNwcLogs(): List<NwcRequestLog> =
        withContext(dispatchers.io()) {
            walletDatabase.nwcLogs().getAllLogs().map { it.asDomain() }
        }
}
