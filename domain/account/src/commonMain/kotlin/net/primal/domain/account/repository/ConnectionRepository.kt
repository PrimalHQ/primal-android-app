package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.RemoteAppConnection
import net.primal.domain.account.model.TrustLevel

interface ConnectionRepository {
    fun observeAllConnections(signerPubKey: String): Flow<List<RemoteAppConnection>>

    fun observeConnection(clientPubKey: String): Flow<RemoteAppConnection?>

    suspend fun getAllConnections(signerPubKey: String): List<RemoteAppConnection>

    suspend fun getAllAutoStartConnections(signerPubKey: String): List<RemoteAppConnection>

    suspend fun deleteConnectionAndData(clientPubKey: String)

    suspend fun removeConnectionsByUserPubKey(userPubKey: String)

    suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<RemoteAppConnection>

    suspend fun insertOrReplaceConnection(secret: String, connection: RemoteAppConnection)

    suspend fun getUserPubKey(clientPubKey: String): Result<String>

    suspend fun canProcessMethod(permissionId: String, clientPubKey: String): Boolean

    suspend fun updateConnectionName(clientPubKey: String, name: String)

    suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean)

    suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel): Result<Unit>
}
