package net.primal.data.account.repository.repository

import net.primal.domain.account.model.Connection
import net.primal.domain.account.repository.AccountRepository

class AccountRepositoryImpl : AccountRepository {
    override suspend fun getAllConnections(): List<Connection> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConnection(connectionId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConnectionsByUser(userPubKey: String) {
        TODO("Not yet implemented")
    }
}
