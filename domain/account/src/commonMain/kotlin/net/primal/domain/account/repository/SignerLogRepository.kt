package net.primal.domain.account.repository

import net.primal.domain.account.model.SignerLog

interface SignerLogRepository {
    suspend fun saveLog(log: SignerLog)
}
