package net.primal.domain.account

import net.primal.core.utils.Result

interface TsunamiWalletAccountRepository {

    suspend fun createWallet(userId: String, walletKey: String): Result<String>
}
