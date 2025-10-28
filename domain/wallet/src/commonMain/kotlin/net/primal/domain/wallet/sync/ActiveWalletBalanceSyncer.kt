package net.primal.domain.wallet.sync

interface ActiveWalletBalanceSyncer : WalletDataSyncer {

    fun getCurrentWalletId(): String?
}
