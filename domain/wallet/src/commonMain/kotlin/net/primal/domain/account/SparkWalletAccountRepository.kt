package net.primal.domain.account

import net.primal.core.utils.Result

interface SparkWalletAccountRepository {

    suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit>

    suspend fun hasPersistedWallet(userId: String): Boolean

    suspend fun getPersistedSeedWords(userId: String): List<String>

    suspend fun persistSeedWords(
        userId: String,
        walletId: String,
        seedWords: String,
    ): Result<Unit>

    suspend fun isWalletBackedUp(walletId: String): Boolean

    suspend fun markWalletAsBackedUp(walletId: String): Result<Unit>

    suspend fun deleteSparkWalletByUserId(userId: String): Result<String>
}
