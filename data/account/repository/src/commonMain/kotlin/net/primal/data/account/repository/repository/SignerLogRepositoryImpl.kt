package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asLocal
import net.primal.domain.account.model.SignerLog
import net.primal.domain.account.repository.SignerLogRepository

class SignerLogRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SignerLogRepository {
    override suspend fun saveLog(log: SignerLog) {
        withContext(dispatchers.io()) {
            database.signerLogs().upsert(data = log.asLocal())
        }
    }
}
