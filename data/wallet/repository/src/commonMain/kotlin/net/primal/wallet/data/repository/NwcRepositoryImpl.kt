package net.primal.wallet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcConnectionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.asDO

internal class NwcRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: WalletDatabase,
) : NwcRepository {

    override suspend fun getConnections(userId: String): List<NwcConnection> =
        with(dispatcherProvider.io()) {
            database.nwcConnections()
                .getAllConnectionsByUser(userId = userId)
                .map { it.asDO() }
        }

    override suspend fun observeConnections(userId: String): Flow<List<NwcConnection>> =
        database.nwcConnections()
            .observeAllConnectionsByUser(userId = userId)
            .map { list -> list.map { it.asDO() } }

    override suspend fun createNewWalletConnection(
        userId: String,
        walletId: String,
        appName: String,
        dailyBudget: Long?,
    ): Result<String> =
        with(dispatcherProvider.io()) {
            val secretKeyPair = CryptoUtils.generateHexEncodedKeypair()
            val serviceKeyPair = CryptoUtils.generateHexEncodedKeypair()

            val walletInfo = database.wallet().findWalletInfo(walletId = walletId)
                ?: return Result.failure<String>(IllegalArgumentException("Couldn't find given wallet id."))

            database.nwcConnections().upsert(
                data = NwcConnectionData(
                    secretPubKey = secretKeyPair.pubKey,
                    walletId = walletId,
                    userId = userId,
                    servicePubKey = serviceKeyPair.pubKey,
                    servicePrivateKey = serviceKeyPair.privateKey.asEncryptable(),
                    relay = DEFAULT_NWC_RELAY.asEncryptable(),
                    appName = appName.asEncryptable(),
                    dailyBudgetSats = dailyBudget?.asEncryptable(),
                ),
            )

            buildNwcString(
                servicePubKey = serviceKeyPair.pubKey,
                secret = secretKeyPair.privateKey,
                lightningAddress = walletInfo.lightningAddress?.decrypted,
            ).asSuccess()
        }

    private fun buildNwcString(
        servicePubKey: String,
        secret: String,
        lightningAddress: String?,
    ) = "$NWC_PROTOCOL$servicePubKey?$RELAY_PARAM=$DEFAULT_NWC_RELAY&$SECRET_PARAM=$secret".run {
        if (lightningAddress != null) {
            "$this&$LUD16_PARAM=$lightningAddress"
        } else {
            this
        }
    }

    companion object {
        private const val DEFAULT_NWC_RELAY = "wss://relay.primal.net"

        private const val NWC_PROTOCOL = "nostr+walletconnect://"
        private const val RELAY_PARAM = "relay"
        private const val SECRET_PARAM = "secret"
        private const val LUD16_PARAM = "lud16"
    }
}
