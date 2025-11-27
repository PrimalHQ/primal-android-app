package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.model.TrustLevel

interface ConnectionRepository {
    fun observeAllConnections(signerPubKey: String): Flow<List<AppConnection>>

    fun observeConnection(connectionId: String): Flow<AppConnection?>

    suspend fun getAllConnections(signerPubKey: String): List<AppConnection>

    suspend fun getAllAutoStartConnections(signerPubKey: String): List<AppConnection>

    suspend fun deleteConnection(connectionId: String)

    suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<AppConnection>

    suspend fun deleteConnectionsByUser(userPubKey: String)

    suspend fun saveConnection(secret: String, connection: AppConnection)

    suspend fun getUserPubKey(clientPubKey: String): Result<String>

    suspend fun canProcessMethod(permissionId: String, clientPubKey: String): Boolean

    suspend fun updateConnectionName(connectionId: String, name: String)

    suspend fun updateConnectionAutoStart(connectionId: String, autoStart: Boolean)

    suspend fun updateTrustLevel(connectionId: String, trustLevel: TrustLevel)

    suspend fun updatePermission(
        connectionId: String,
        permissionId: String,
        action: PermissionAction,
    )
}
