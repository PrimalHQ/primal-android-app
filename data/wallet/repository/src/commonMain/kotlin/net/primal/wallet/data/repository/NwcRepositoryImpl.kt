package net.primal.wallet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.model.NostrWalletConnection
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.NostrWalletConnectionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.asDO

class NwcRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: WalletDatabase,
) : NwcRepository {
    override suspend fun getConnections(userId: String): List<NostrWalletConnection> =
        with(dispatcherProvider.io()) {
            database.nwcConnections()
                .getAllConnectionsByUser(userId = userId)
                .map { it.asDO() }
        }

    override suspend fun observeConnections(userId: String): Flow<List<NostrWalletConnection>> =
        database.nwcConnections()
            .observeAllConnectionsByUser(userId = userId)
            .map { list -> list.map { it.asDO() } }

    override suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: Long?,
    ): Result<String> =
        with(dispatcherProvider.io()) {
            val secretKeyPair = CryptoUtils.generateHexEncodedKeypair()
            val serviceKeyPair = CryptoUtils.generateHexEncodedKeypair()

            val wallet = database.wallet()
                .findLastUsedWalletByType(userId = userId.asEncryptable(), type = WalletType.TSUNAMI)
                ?: return Result.failure<String>(RuntimeException("Couldn't find Tsunami wallet."))
            /* TODO: we decided to just pick tsunami wallet. This should probably be adjusted? */

            database.nwcConnections().upsert(
                data = NostrWalletConnectionData(
                    secretPubKey = secretKeyPair.pubKey,
                    walletId = wallet.info.walletId,
                    userId = userId,
                    servicePubKey = serviceKeyPair.pubKey,
                    servicePrivateKey = serviceKeyPair.privateKey.asEncryptable(),
                    relay = RELAY.asEncryptable(),
                    appName = appName.asEncryptable(),
                    dailyBudgetSats = dailyBudget?.asEncryptable(),
                ),
            )

            buildNwcString(
                servicePubKey = serviceKeyPair.pubKey,
                secret = secretKeyPair.privateKey,
                lightningAddress = wallet.info.lightningAddress?.decrypted,
            ).asSuccess()
        }

    private fun buildNwcString(
        servicePubKey: String,
        secret: String,
        lightningAddress: String?,
    ) = "$NWC_PROTOCOL$servicePubKey?$RELAY_PARAM=$RELAY&$SECRET_PARAM=$secret".run {
        if (lightningAddress != null) {
            "$this&$LUD16_PARAM=$lightningAddress"
        } else {
            this
        }
    }

    companion object {
        /* TODO: check this relay. Should it be hard-coded, should it be another relay? */
        private const val RELAY = "wss://relay.primal.net"

        private const val NWC_PROTOCOL = "nostr+walletconnect://"
        private const val RELAY_PARAM = "relay"
        private const val SECRET_PARAM = "secret"
        private const val LUD16_PARAM = "lud16"
    }
}
