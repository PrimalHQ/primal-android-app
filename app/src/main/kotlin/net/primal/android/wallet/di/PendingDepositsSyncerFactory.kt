package net.primal.android.wallet.di

import javax.inject.Inject
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.sync.PendingDepositsSyncer
import net.primal.wallet.data.syncer.factory.SyncerFactory

class PendingDepositsSyncerFactory @Inject constructor(
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletManager: SparkWalletManager,
) {

    fun create(userId: String): PendingDepositsSyncer {
        return SyncerFactory.createPendingDepositsSyncer(
            userId = userId,
            walletAccountRepository = walletAccountRepository,
            sparkWalletManager = sparkWalletManager,
        )
    }
}
