package net.primal.domain.connections.primal

import net.primal.domain.connections.primal.model.PrimalNwcConnection
import net.primal.domain.connections.primal.model.PrimalNwcConnectionInfo

interface PrimalWalletNwcRepository {

    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo>

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: String?,
    ): PrimalNwcConnection

    suspend fun revokeConnection(userId: String, nwcPubkey: String)
}
