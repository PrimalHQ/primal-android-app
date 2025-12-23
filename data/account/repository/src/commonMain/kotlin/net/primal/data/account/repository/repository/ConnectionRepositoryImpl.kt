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
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.PermissionAction
import net.primal.data.account.local.dao.apps.remote.RemoteAppConnectionData
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.api.WellKnownApi
import net.primal.data.account.remote.utils.PERM_ID_CONNECT
import net.primal.data.account.remote.utils.PERM_ID_PING
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.asPO
import net.primal.domain.account.model.RemoteAppConnection
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

class ConnectionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
    private val wellKnownApi: WellKnownApi,
) : ConnectionRepository {

    override fun observeAllConnections(signerPubKey: String): Flow<List<RemoteAppConnection>> =
        database.remoteAppConnections().observeAllConnections(signerPubKey = signerPubKey)
            .map { connections -> connections.map { it.asDomain() } }

    override fun observeConnection(clientPubKey: String): Flow<RemoteAppConnection?> {
        return database.remoteAppConnections().observeConnection(clientPubKey = clientPubKey)
            .map { it?.asDomain() }
    }

    override suspend fun getAllConnections(signerPubKey: String): List<RemoteAppConnection> =
        withContext(dispatchers.io()) {
            database.remoteAppConnections().getAll(signerPubKey = signerPubKey)
                .map { it.asDomain() }
        }

    override suspend fun getAllAutoStartConnections(signerPubKey: String): List<RemoteAppConnection> =
        withContext(dispatchers.io()) {
            database.remoteAppConnections().getAllAutoStartConnections(signerPubKey = signerPubKey)
                .map { it.asDomain() }
        }

    override suspend fun deleteConnectionAndData(clientPubKey: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                deleteEverythingForClientPubKey(clientPubKey)
            }
        }

    override suspend fun removeConnectionsByUserPubKey(userPubKey: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                database.remoteAppConnections().getConnectionsByUser(userPubKey = userPubKey)
                    .forEach { connection ->
                        deleteEverythingForClientPubKey(clientPubKey = connection.data.clientPubKey)
                    }
            }
        }

    override suspend fun getConnectionByClientPubKey(clientPubKey: String): Result<RemoteAppConnection> =
        withContext(dispatchers.io()) {
            database.remoteAppConnections().getConnection(clientPubKey = clientPubKey)
                ?.asDomain()?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate connection with given `clientPubKey`."))
        }

    override suspend fun insertOrReplaceConnection(secret: String, connection: RemoteAppConnection) =
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
                    val action = database.appPermissions()
                        .findPermission(permissionId = permissionId, appIdentifier = connection.clientPubKey)
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
            database.remoteAppConnections().getConnection(clientPubKey = clientPubKey)
                ?.data?.userPubKey?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate user pubkey for client pubkey."))
        }

    override suspend fun updateConnectionName(clientPubKey: String, name: String) {
        withContext(dispatchers.io()) {
            database.remoteAppConnections().updateConnectionName(clientPubKey, name.asEncryptable())
        }
    }

    override suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean) {
        withContext(dispatchers.io()) {
            database.remoteAppConnections().updateConnectionAutoStart(clientPubKey, autoStart)
        }
    }

    override suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel) =
        withContext(dispatchers.io()) {
            runCatching {
                database.withTransaction {
                    if (trustLevel == TrustLevel.Medium) {
                        val existingPermissions = database.appPermissions()
                            .findPermissionsByAppIdentifier(appIdentifier = clientPubKey)

                        if (existingPermissions.isEmpty()) {
                            val newPermissions = wellKnownApi
                                .getMediumTrustPermissions().allowPermissions
                                .map {
                                    AppPermissionData(
                                        permissionId = it,
                                        appIdentifier = clientPubKey,
                                        action = PermissionAction.Approve,
                                    )
                                }

                            database.appPermissions().upsertAll(data = newPermissions)
                        }
                    }

                    database.remoteAppConnections().updateTrustLevel(clientPubKey, trustLevel.asPO())
                }
            }
        }

    private suspend inline fun insertAppConnection(secret: String, connection: RemoteAppConnection) {
        database.remoteAppConnections().insert(
            data = RemoteAppConnectionData(
                clientPubKey = connection.clientPubKey,
                signerPubKey = connection.signerPubKey,
                userPubKey = connection.userPubKey,
                relays = connection.relays.asEncryptable(),
                secret = secret.asEncryptable(),
                name = connection.name?.asEncryptable(),
                url = connection.url?.asEncryptable(),
                image = connection.image?.asEncryptable(),
                autoStart = connection.autoStart,
                trustLevel = connection.trustLevel.asPO(),
            ),
        )
        database.appPermissions().upsertAll(data = connection.permissions.map { it.asPO() })
    }

    private suspend inline fun deleteEverythingForClientPubKey(clientPubKey: String) {
        database.appSessions().deleteSessions(clientPubKey = clientPubKey)
        database.appPermissions().deletePermissions(appIdentifier = clientPubKey)
        database.remoteAppConnections().deleteConnection(clientPubKey = clientPubKey)
        database.remoteAppSessionEvents().deleteEvents(clientPubKey = clientPubKey)
        database.remoteAppPendingNostrEvents().deleteByClientPubKey(clientPubKey = clientPubKey)
    }
}
