package net.primal.wallet.data.handler.factory

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.profile.ProfileRepository
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.service.WalletService

internal object HandlerFactory {

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
