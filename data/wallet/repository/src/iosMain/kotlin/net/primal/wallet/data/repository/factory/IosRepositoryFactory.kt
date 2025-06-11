package net.primal.wallet.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.domain.WalletRepository

typealias WalletRepositoryFactory = IosRepositoryFactory

object IosRepositoryFactory : RepositoryFactory {

    private val dispatcherProvider = IOSDispatcherProvider()

    private val walletDatabase by lazy {
        LocalDatabaseFactory.createDatabase<WalletDatabase>(databaseName = "wallet_database.db")
    }

    override fun createWalletRepository(walletPrimalApiClient: PrimalApiClient): WalletRepository {
        throw NotImplementedError()
    }
}
