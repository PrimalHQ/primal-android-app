package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.SparkWalletData
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletUserLink
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.validator.RecoveryPhraseValidator

internal class SparkWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
) : SparkWalletAccountRepository {

    private val recoveryPhraseValidator: RecoveryPhraseValidator = RecoveryPhraseValidator()

    override suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                val walletStatus = walletApi.getWalletStatus(userId)
                val lightningAddress = walletStatus.lightningAddress?.takeIf { walletStatus.hasSparkWallet }
                walletDatabase.withTransaction {
                    walletDatabase.wallet().insertOrIgnoreWalletInfo(
                        info = WalletInfo(
                            walletId = walletId,
                            type = WalletType.SPARK,
                        ),
                    )
                    walletDatabase.wallet().insertWalletUserLink(
                        WalletUserLink(userId = userId, walletId = walletId),
                    )
                    walletDatabase.wallet().updateLinkLightningAddress(
                        userId = userId,
                        walletId = walletId,
                        lightningAddress = lightningAddress?.asEncryptable(),
                    )
                }
            }
        }

    override suspend fun registerSparkWallet(userId: String, walletId: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                walletApi.registerSparkWallet(userId = userId, sparkWalletId = walletId)
            }
        }

    override suspend fun unregisterSparkWallet(userId: String, walletId: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                walletApi.unregisterSparkWallet(userId = userId, sparkWalletId = walletId)
            }
        }

    override suspend fun findPersistedWalletId(userId: String): String? =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findAllSparkWalletDataByUserId(userId)
                .firstOrNull()?.walletId
        }

    override suspend fun findAllPersistedWalletIds(userId: String): List<String> =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findAllSparkWalletDataByUserId(userId)
                .map { it.walletId }
        }

    override suspend fun getPersistedSeedWords(walletId: String): Result<List<String>> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                val data = walletDatabase.wallet().findSparkWalletData(walletId)
                    ?: error("No spark wallet data found for walletId=$walletId")

                data.seedWords.decrypted.toSeedWordList()
            }
        }

    override suspend fun persistSeedWords(walletId: String, seedWords: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                if (!recoveryPhraseValidator.isValid(seedWords)) {
                    error("Invalid recovery phrase: expected 12, 15, 18, 21, or 24 valid BIP39 words.")
                }
                walletDatabase.wallet().upsertSparkWalletData(
                    SparkWalletData(
                        walletId = walletId,
                        seedWords = seedWords.asEncryptable(),
                    ),
                )
            }
        }

    override suspend fun getLightningAddress(userId: String, walletId: String): String? =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findWalletUserLink(userId, walletId)?.lightningAddress?.decrypted
        }

    override suspend fun isRegistered(userId: String, walletId: String): Boolean =
        withContext(dispatcherProvider.io()) {
            val link = walletDatabase.wallet().findWalletUserLink(userId, walletId)
            link?.lightningAddress?.decrypted?.isNotBlank() == true
        }

    override suspend fun isWalletBackedUp(walletId: String): Boolean =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findSparkWalletData(walletId)?.backedUp ?: false
        }

    override suspend fun markWalletAsBackedUp(walletId: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                walletDatabase.wallet().updateSparkWalletBackedUp(walletId, backedUp = true)
            }
        }

    override suspend fun isPrimalTxsMigrationCompleted(walletId: String): Boolean =
        withContext(dispatcherProvider.io()) {
            val sparkWalletData = walletDatabase.wallet().findSparkWalletData(walletId)
            // Returns true if migration completed OR not needed (new user)
            // null = new user (no migration needed), true = fully migrated, false = in progress
            sparkWalletData?.primalTxsMigrated != false
        }

    override suspend fun ensureWalletInfoExists(userId: String, walletId: String) {
        withContext(dispatcherProvider.io()) {
            walletDatabase.withTransaction {
                walletDatabase.wallet().insertOrIgnoreWalletInfo(
                    WalletInfo(
                        walletId = walletId,
                        type = WalletType.SPARK,
                    ),
                )
                walletDatabase.wallet().insertWalletUserLink(
                    WalletUserLink(userId = userId, walletId = walletId),
                )
            }
        }
    }
}

private fun String.toSeedWordList(): List<String> = split(" ").filter { it.isNotBlank() }
