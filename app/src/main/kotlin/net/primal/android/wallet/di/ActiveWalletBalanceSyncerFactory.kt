package net.primal.android.wallet.di

import javax.inject.Inject
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.sync.ActiveWalletBalanceSyncer
import net.primal.wallet.data.syncer.factory.SyncerFactory

class ActiveWalletBalanceSyncerFactory @Inject constructor(
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
) {

    fun create(userId: String): ActiveWalletBalanceSyncer {
        return SyncerFactory.createActiveWalletBalanceSyncer(
            userId = userId,
            walletRepository = walletRepository,
            walletAccountRepository = walletAccountRepository,
        )
    }
}
