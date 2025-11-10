package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.contains
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.dao.AppConnectionData
import net.primal.data.account.local.dao.PermissionAction
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.utils.PERM_ID_CONNECT
import net.primal.data.account.remote.utils.PERM_ID_PING
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.asPO
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

class ConnectionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : ConnectionRepository {
    override fun observeAllConnections(signerPubKey: String): Flow<List<AppConnection>> =
        database.connections().observeAllConnections(signerPubKey = signerPubKey.asEncryptable())
            .map { connections -> connections.map { it.asDomain() } }

    override fun observeConnection(connectionId: String): Flow<AppConnection?> {
        return database.connections().observeConnection(connectionId = connectionId)
            .map { it?.asDomain() }
    }

    override suspend fun getAllConnections(signerPubKey: String): List<AppConnection> =
        withContext(dispatchers.io()) {
            database.connections().getAll(signerPubKey = signerPubKey.asEncryptable())
                .map { it.asDomain() }
        }

    override suspend fun deleteConnection(connectionId: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                database.sessions().deleteSessionsByConnectionId(connectionId = connectionId)
                database.permissions().deletePermissionsByConnectionId(connectionId = connectionId)
                database.connections().deleteConnection(connectionId = connectionId)
            }
        }

    override suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<AppConnection> =
        withContext(dispatchers.io()) {
            database.connections().getConnectionByClientPubKey(clientPubKey = clientPubKey.asEncryptable())
                ?.asDomain()?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate connection with given `clientPubKey`."))
        }

    override suspend fun deleteConnectionsByUser(userPubKey: String) =
        withContext(dispatchers.io()) {
            database.connections().deleteConnectionsByUser(userPubKey = userPubKey.asEncryptable())
        }

    override suspend fun saveConnection(secret: String, connection: AppConnection) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                database.connections().upsertAll(
                    data = listOf(
                        AppConnectionData(
                            connectionId = connection.connectionId,
                            relays = connection.relays.asEncryptable(),
                            secret = secret.asEncryptable(),
                            name = connection.name?.asEncryptable(),
                            url = connection.url?.asEncryptable(),
                            image = connection.image?.asEncryptable(),
                            clientPubKey = connection.clientPubKey.asEncryptable(),
                            signerPubKey = connection.signerPubKey.asEncryptable(),
                            userPubKey = connection.userPubKey.asEncryptable(),
                            autoStart = connection.autoStart,
                            trustLevel = connection.trustLevel.asPO(),
                        ),
                    ),
                )

                database.permissions().upsertAll(data = connection.permissions.map { it.asPO() })
            }
        }

    override suspend fun canProcessMethod(permissionId: String, clientPubKey: String): Boolean =
        withContext(dispatchers.io()) {
            val connection = getConnectionByClientPubKey(clientPubKey = clientPubKey).getOrNull()
                ?: return@withContext false

            when (connection.trustLevel) {
                TrustLevel.Full -> true
                TrustLevel.Medium -> {
                    val action = resolvePermissionAction(
                        permissionId = permissionId,
                        connectionId = connection.connectionId,
                    )

                    when (action) {
                        PermissionAction.Approve -> true
                        PermissionAction.Deny, PermissionAction.Ask, null -> false
                    }
                }

                TrustLevel.Low -> false
            }
        }

    private suspend fun resolvePermissionAction(permissionId: String, connectionId: String) =
        when (permissionId) {
            /* TODO(marko): should we auto approve connect and ping requests?
                 - Connect requests have proven to be client's way of starting up "session".
                      If our user has started the session he doesn't need to get prompted to approve it again...
                 - Ping requests shouldn't be known to user. This is client's way of checking if we are alive.
             */
            in Regex(PERM_ID_CONNECT), in Regex(PERM_ID_PING) -> PermissionAction.Approve
            else -> database.permissions()
                .findPermission(permissionId = permissionId, connectionId = connectionId)
                ?.action
        }

    override suspend fun getUserPubKey(clientPubKey: String): Result<String> =
        withContext(dispatchers.io()) {
            database.connections()
                .getConnectionByClientPubKey(clientPubKey = clientPubKey.asEncryptable())
                ?.data?.userPubKey?.decrypted?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate user pubkey for client pubkey."))
        }

    override suspend fun updateConnectionName(connectionId: String, name: String) {
        withContext(dispatchers.io()) {
            database.connections().updateConnectionName(connectionId, name.asEncryptable())
        }
    }

    override suspend fun updateConnectionAutoStart(connectionId: String, autoStart: Boolean) {
        withContext(dispatchers.io()) {
            database.connections().updateConnectionAutoStart(connectionId, autoStart)
        }
    }
}
