package net.primal.domain.account

import net.primal.core.utils.Result

interface TsunamiWalletAccountRepository {

    suspend fun initializeWallet(userId: String, walletKey: String): Result<String>

    suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit>

    suspend fun terminateWallet(walletId: String): Result<Unit>
}
