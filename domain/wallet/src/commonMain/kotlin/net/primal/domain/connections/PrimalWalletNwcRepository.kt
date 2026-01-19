package net.primal.domain.connections

import net.primal.domain.connections.model.NwcConnection
import net.primal.domain.connections.model.NwcConnectionInfo

interface PrimalWalletNwcRepository {

    suspend fun getConnections(userId: String): List<NwcConnectionInfo>

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: String?,
    ): NwcConnection

    suspend fun revokeConnection(userId: String, nwcPubkey: String)
}
