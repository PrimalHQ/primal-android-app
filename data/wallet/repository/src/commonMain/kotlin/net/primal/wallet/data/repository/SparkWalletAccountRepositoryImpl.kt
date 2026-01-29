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
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.validator.RecoveryPhraseValidator

internal class SparkWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
) : SparkWalletAccountRepository {

    private val recoveryPhraseValidator: RecoveryPhraseValidator = RecoveryPhraseValidator()

    override suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                walletDatabase.withTransaction {
                    walletDatabase.wallet().insertOrIgnoreWalletInfo(
                        info = WalletInfo(
                            walletId = walletId,
                            userId = userId,
                            lightningAddress = null,
                            type = WalletType.SPARK,
                        ),
                    )
                }
            }
        }

    override suspend fun hasPersistedWallet(userId: String): Boolean =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findAllSparkWalletDataByUserId(userId).isNotEmpty()
        }

    override suspend fun getPersistedSeedWords(userId: String): List<String> =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findAllSparkWalletDataByUserId(userId)
                .map { it.seedWords.decrypted }
        }

    override suspend fun persistSeedWords(
        userId: String,
        walletId: String,
        seedWords: String,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcherProvider.io()) {
                if (!recoveryPhraseValidator.isValid(seedWords)) {
                    error("Invalid recovery phrase: expected 12, 15, 18, 21, or 24 valid BIP39 words.")
                }
                walletDatabase.wallet().upsertSparkWalletData(
                    SparkWalletData(
                        walletId = walletId,
                        userId = userId,
                        seedWords = seedWords.asEncryptable(),
                    ),
                )
            }
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
}
