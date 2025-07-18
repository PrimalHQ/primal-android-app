package net.primal.wallet.data.handler.factory

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.WalletService
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.db.WalletDatabase

object HandlerFactory {

    fun createTransactionsHandler(
        dispatchers: DispatcherProvider,
        primalWalletService: WalletService,
        nostrWalletService: WalletService,
        walletDatabase: WalletDatabase,
        profileRepository: ProfileRepository,
    ) = TransactionsHandler(
        dispatchers = dispatchers,
        primalWalletService = primalWalletService,
        nostrWalletService = nostrWalletService,
        walletDatabase = walletDatabase,
        profileRepository = profileRepository,
    )
}
