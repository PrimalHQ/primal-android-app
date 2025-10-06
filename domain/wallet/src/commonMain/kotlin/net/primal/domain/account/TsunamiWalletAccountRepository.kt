package net.primal.domain.account

interface TsunamiWalletAccountRepository {

    suspend fun createWallet(userId: String, walletKey: String)
}
