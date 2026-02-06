package net.primal.domain.connections.nostr

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.connections.nostr.model.NwcConnection

interface NwcRepository {
    suspend fun getConnections(userId: String): List<NwcConnection>

    suspend fun observeConnections(userId: String): Flow<List<NwcConnection>>

    suspend fun createNewWalletConnection(
        userId: String,
        walletId: String,
        appName: String,
        dailyBudget: Long?,
    ): Result<String>

    suspend fun revokeConnection(userId: String, secretPubKey: String)
}
