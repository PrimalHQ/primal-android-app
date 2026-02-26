package net.primal.domain.account

import net.primal.core.utils.Result

interface SparkWalletAccountRepository {

    suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit>

    suspend fun registerSparkWallet(userId: String, walletId: String): Result<Unit>

    suspend fun unregisterSparkWallet(userId: String, walletId: String): Result<Unit>

    suspend fun findPersistedWalletId(userId: String): String?

    suspend fun getPersistedSeedWords(walletId: String): Result<List<String>>

    suspend fun persistSeedWords(
        userId: String,
        walletId: String,
        seedWords: String,
    ): Result<Unit>

    suspend fun getLightningAddress(walletId: String): String?

    suspend fun isRegistered(walletId: String): Boolean

    suspend fun isWalletBackedUp(walletId: String): Boolean

    suspend fun markWalletAsBackedUp(walletId: String): Result<Unit>

    suspend fun deleteSparkWalletByUserId(userId: String): Result<String>

    suspend fun isPrimalTxsMigrationCompleted(walletId: String): Boolean

    suspend fun clearPrimalTxsMigrationState(walletId: String)
}
