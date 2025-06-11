package net.primal.wallet.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.wallet.domain.WalletRepository

internal interface RepositoryFactory {

    fun createWalletRepository(walletPrimalApiClient: PrimalApiClient): WalletRepository
}
