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

    override fun observeConnection(clientPubKey: String): Flow<AppConnection?> {
        return database.connections().observeConnection(clientPubKey = clientPubKey)
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

    override suspend fun deleteConnectionAndData(clientPubKey: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                deleteEverythingForClientPubKey(clientPubKey)
            }
        }

    override suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<AppConnection> =
        withContext(dispatchers.io()) {
            database.connections().getConnection(clientPubKey = clientPubKey)
                ?.asDomain()?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate connection with given `clientPubKey`."))
        }

    override suspend fun insertOrReplaceConnection(secret: String, connection: AppConnection) =
        withContext(dispatchers.io()) {
            try {
                database.withTransaction {
                    insertAppConnection(secret = secret, connection = connection)
                }
            } catch (_: SQLiteException) {
                database.withTransaction {
                    deleteEverythingForClientPubKey(clientPubKey = connection.clientPubKey)
                    insertAppConnection(secret = secret, connection = connection)
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
                        .findPermission(permissionId = permissionId, clientPubKey = connection.clientPubKey)
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
            database.connections().getConnection(clientPubKey = clientPubKey)
                ?.data?.userPubKey?.decrypted?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate user pubkey for client pubkey."))
        }

    override suspend fun updateConnectionName(clientPubKey: String, name: String) {
        withContext(dispatchers.io()) {
            database.connections().updateConnectionName(clientPubKey, name.asEncryptable())
        }
    }

    override suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean) {
        withContext(dispatchers.io()) {
            database.connections().updateConnectionAutoStart(clientPubKey, autoStart)
        }
    }

    override suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel) =
        withContext(dispatchers.io()) {
            runCatching {
                database.withTransaction {
                    if (trustLevel == TrustLevel.Medium) {
                        val existingPermissions = database.permissions()
                            .findPermissionsByClientPubKey(clientPubKey = clientPubKey)

                        if (existingPermissions.isEmpty()) {
                            val newPermissions = wellKnownApi
                                .getMediumTrustPermissions().allowPermissions
                                .map {
                                    AppPermissionData(
                                        permissionId = it,
                                        clientPubKey = clientPubKey,
                                        action = PermissionAction.Approve,
                                    )
                                }

                            database.permissions().upsertAll(data = newPermissions)
                        }
                    }

                    database.connections().updateTrustLevel(clientPubKey, trustLevel.asPO())
                }
            }
        }

    private suspend inline fun insertAppConnection(secret: String, connection: AppConnection) {
        database.connections().insert(
            data = AppConnectionData(
                clientPubKey = connection.clientPubKey,
                signerPubKey = connection.signerPubKey.asEncryptable(),
                userPubKey = connection.userPubKey.asEncryptable(),
                relays = connection.relays.asEncryptable(),
                secret = secret.asEncryptable(),
                name = connection.name?.asEncryptable(),
                url = connection.url?.asEncryptable(),
                image = connection.image?.asEncryptable(),
                autoStart = connection.autoStart,
                trustLevel = connection.trustLevel.asPO(),
            ),
        )
        database.permissions().upsertAll(data = connection.permissions.map { it.asPO() })
    }

    private suspend inline fun deleteEverythingForClientPubKey(clientPubKey: String) {
        database.pendingNostrEvents().deleteByClientPubKey(clientPubKey = clientPubKey)
        database.sessionEvents().deleteEvents(clientPubKey = clientPubKey)
        database.sessions().deleteSessions(clientPubKey = clientPubKey)
        database.permissions().deletePermissions(clientPubKey = clientPubKey)
        database.connections().deleteConnection(clientPubKey = clientPubKey)
    }
}
