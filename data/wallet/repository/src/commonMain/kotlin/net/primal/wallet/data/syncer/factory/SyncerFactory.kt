package net.primal.wallet.data.syncer.factory

import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.sync.ActiveWalletBalanceSyncer
import net.primal.wallet.data.syncer.balance.ActiveWalletBalanceSyncerImpl

object SyncerFactory {

    private val dispatcherProvider = createDispatcherProvider()

    fun createActiveWalletBalanceSyncer(
        userId: String,
        walletRepository: WalletRepository,
        walletAccountRepository: WalletAccountRepository,
    ): ActiveWalletBalanceSyncer {
        return ActiveWalletBalanceSyncerImpl(
            dispatcherProvider = dispatcherProvider,
            userId = userId,
            walletAccountRepository = walletAccountRepository,
            walletRepository = walletRepository,
        )
    }
}
