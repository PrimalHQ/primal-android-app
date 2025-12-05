package net.primal.data.account.repository.repository

import androidx.sqlite.SQLiteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.contains
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.AppConnectionData
import net.primal.data.account.local.dao.AppPermissionData
import net.primal.data.account.local.dao.PermissionAction
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.api.WellKnownApi
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
    private val wellKnownApi: WellKnownApi,
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

    override suspend fun getAllAutoStartConnections(signerPubKey: String): List<AppConnection> =
        withContext(dispatchers.io()) {
            database.connections().getAllAutoStartConnections(signerPubKey = signerPubKey.asEncryptable())
                .map { it.asDomain() }
        }

    override suspend fun deleteConnection(connectionId: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                deleteConnectionInternal(connectionId)
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
            try {
                database.withTransaction {
                    saveConnectionInternal(secret, connection)
                }
            } catch (_: SQLiteException) {
                database.withTransaction {
                    val existing = database.connections().getConnectionByClientPubKey(
                        clientPubKey = connection.clientPubKey.asEncryptable(),
                    )

                    if (existing != null) {
                        deleteConnectionInternal(existing.data.connectionId)
                        saveConnectionInternal(secret, connection)
                    }
                }
            }
        }

    override suspend fun canProcessMethod(permissionId: String, clientPubKey: String): Boolean =
        withContext(dispatchers.io()) {
            val connection = getConnectionByClientPubKey(clientPubKey = clientPubKey).getOrNull()
                ?: return@withContext false

            if (permissionId in Regex(PERM_ID_CONNECT) || permissionId in Regex(PERM_ID_PING)) {
                return@withContext true
            }

            when (connection.trustLevel) {
                TrustLevel.Full -> true
                TrustLevel.Medium -> {
                    val action = database.permissions()
                        .findPermission(permissionId = permissionId, connectionId = connection.connectionId)
                        ?.action

                    when (action) {
                        PermissionAction.Approve -> true
                        PermissionAction.Deny, PermissionAction.Ask, null -> false
                    }
                }

                TrustLevel.Low -> false
            }
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

    override suspend fun updateTrustLevel(connectionId: String, trustLevel: TrustLevel) =
        withContext(dispatchers.io()) {
            runCatching {
                database.withTransaction {
                    if (trustLevel == TrustLevel.Medium) {
                        val existingPermissions = database.permissions()
                            .findPermissionsByConnectionId(connectionId = connectionId)

                        if (existingPermissions.isEmpty()) {
                            val newPermissions = wellKnownApi
                                .getMediumTrustPermissions().allowPermissions
                                .map {
                                    AppPermissionData(
                                        permissionId = it,
                                        connectionId = connectionId,
                                        action = PermissionAction.Approve,
                                    )
                                }

                            database.permissions().upsertAll(data = newPermissions)
                        }
                    }

                    database.connections().updateTrustLevel(connectionId, trustLevel.asPO())
                }
            }
        }

    private suspend fun saveConnectionInternal(secret: String, connection: AppConnection) {
        database.connections().insert(
            data = AppConnectionData(
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
        )
        database.permissions().upsertAll(data = connection.permissions.map { it.asPO() })
    }

    private suspend fun deleteConnectionInternal(connectionId: String) {
        database.sessionEvents().deleteEventsByConnectionId(connectionId = connectionId)
        database.sessions().deleteSessionsByConnectionId(connectionId = connectionId)
        database.permissions().deletePermissionsByConnectionId(connectionId = connectionId)
        database.connections().deleteConnection(connectionId = connectionId)
    }
}
