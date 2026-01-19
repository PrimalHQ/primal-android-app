package net.primal.domain.account

import kotlinx.coroutines.flow.Flow
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType

interface WalletAccountRepository {
    suspend fun setActiveWallet(userId: String, walletId: String)

    suspend fun clearActiveWallet(userId: String)

    fun observeWalletsByUser(userId: String): Flow<List<Wallet>>

    suspend fun findLastUsedWallet(userId: String, type: WalletType): Wallet?

    suspend fun getActiveWallet(userId: String): Wallet?

    fun observeActiveWallet(userId: String): Flow<Wallet?>

    fun observeActiveWalletId(userId: String): Flow<String?>
}
