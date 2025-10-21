package net.primal.domain.account.repository

import net.primal.core.utils.Result
import net.primal.domain.account.model.AppConnection

interface ConnectionRepository {
    suspend fun getAllConnections(signerPubKey: String): List<AppConnection>

    suspend fun deleteConnection(connectionId: String)

    suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<AppConnection>

    suspend fun deleteConnectionsByUser(userPubKey: String)

    suspend fun saveConnection(secret: String, connection: AppConnection)

    suspend fun getUserPubKey(clientPubKey: String): Result<String>
}
