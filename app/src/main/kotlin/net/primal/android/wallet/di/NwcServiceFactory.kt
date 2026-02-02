package net.primal.android.wallet.di

import javax.inject.Inject
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory

class NwcServiceFactory @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    fun create(): NwcService =
        WalletRepositoryFactory.createNwcService(
            walletRepository = walletRepository,
        )
}
