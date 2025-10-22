package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.dao.AppConnectionData
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.shared.data.local.encryption.asEncryptable

class ConnectionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : ConnectionRepository {
    override suspend fun getAllConnections(signerPubKey: String): List<AppConnection> =
        withContext(dispatchers.io()) {
            database.connections().getAll(signerPubKey = signerPubKey.asEncryptable())
                .map { it.asDomain() }
        }

    override suspend fun deleteConnection(connectionId: String) =
        withContext(dispatchers.io()) {
            database.connections().deleteConnection(connectionId = connectionId)
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
                    ),
                ),
            )
        }

    override suspend fun getUserPubKey(clientPubKey: String): Result<String> =
        withContext(dispatchers.io()) {
            database.connections().getUserPubKey(clientPubKey = clientPubKey.asEncryptable())?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't locate user pubkey for client pubkey."))
        }
}
