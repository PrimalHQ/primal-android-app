package net.primal.domain.connections.nostr

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.connections.nostr.model.NostrWalletConnection

interface NwcRepository {
    suspend fun getConnections(userId: String): List<NostrWalletConnection>

    suspend fun observeConnections(userId: String): Flow<List<NostrWalletConnection>>

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: Long?,
    ): Result<String>
}
