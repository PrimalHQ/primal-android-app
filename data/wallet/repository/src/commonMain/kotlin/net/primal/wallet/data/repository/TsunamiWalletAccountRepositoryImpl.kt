package net.primal.wallet.data.repository

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.TsunamiWalletAccountRepository
import net.primal.wallet.data.local.db.WalletDatabase

class TsunamiWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
) : TsunamiWalletAccountRepository {

    override suspend fun createWallet(userId: String, walletKey: String) {
    }
}
