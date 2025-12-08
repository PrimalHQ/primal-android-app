package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.TrustLevel

interface ConnectionRepository {
    fun observeAllConnections(signerPubKey: String): Flow<List<AppConnection>>

    fun observeConnection(clientPubKey: String): Flow<AppConnection?>

    suspend fun getAllConnections(signerPubKey: String): List<AppConnection>

    suspend fun getAllAutoStartConnections(signerPubKey: String): List<AppConnection>

    suspend fun deleteConnectionAndData(clientPubKey: String)

    suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<AppConnection>

    suspend fun insertOrReplaceConnection(secret: String, connection: AppConnection)

    suspend fun getUserPubKey(clientPubKey: String): Result<String>

    suspend fun canProcessMethod(permissionId: String, clientPubKey: String): Boolean

    suspend fun updateConnectionName(clientPubKey: String, name: String)

    suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean)

    suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel): Result<Unit>
}
