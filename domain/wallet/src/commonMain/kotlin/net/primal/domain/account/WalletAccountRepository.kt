package net.primal.domain.account

import kotlinx.coroutines.flow.Flow
import net.primal.domain.wallet.UserWallet
import net.primal.domain.wallet.WalletType

interface WalletAccountRepository {
    suspend fun setActiveWallet(userId: String, walletId: String)

    suspend fun clearActiveWallet(userId: String)

    fun observeWalletsByUser(userId: String): Flow<List<UserWallet>>

    suspend fun findLastUsedWallet(userId: String, type: WalletType): UserWallet?

    suspend fun findLastUsedWallet(userId: String, type: Set<WalletType>): UserWallet?

    suspend fun getActiveWallet(userId: String): UserWallet?

    fun observeActiveWallet(userId: String): Flow<UserWallet?>

    fun observeActiveWalletId(userId: String): Flow<String?>
}
