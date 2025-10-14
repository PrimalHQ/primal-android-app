package net.primal.domain.account.repository

import net.primal.domain.account.model.Connection

interface AccountRepository {
    suspend fun getAllConnections(): List<Connection>

    suspend fun deleteConnection(connectionId: String)

    suspend fun deleteConnectionsByUser(userPubKey: String)
}
