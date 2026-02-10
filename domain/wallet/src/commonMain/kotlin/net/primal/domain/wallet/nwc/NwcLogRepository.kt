package net.primal.domain.wallet.nwc

import net.primal.domain.wallet.nwc.model.NwcRequestLog

interface NwcLogRepository {
    suspend fun getNwcLogs(): List<NwcRequestLog>
}
