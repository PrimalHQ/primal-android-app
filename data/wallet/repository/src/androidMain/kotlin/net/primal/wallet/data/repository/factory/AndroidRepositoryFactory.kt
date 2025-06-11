package net.primal.wallet.data.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.AndroidDispatcherProvider
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.encryption.AndroidPlatformKeyStore
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.WalletRepositoryImpl
import net.primal.wallet.domain.WalletRepository

typealias WalletRepositoryFactory = AndroidRepositoryFactory

object AndroidRepositoryFactory : RepositoryFactory {

    private var appContext: Context? = null

    private val dispatcherProvider = AndroidDispatcherProvider()

    private val walletDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        LocalDatabaseFactory.createDatabase<WalletDatabase>(
            context = appContext,
            databaseName = "wallet_database.db",
        )
    }

    fun init(context: Context) {
        this.appContext = context.applicationContext
        AndroidPlatformKeyStore.init(context)
        AppConfigInitializer.init(context)
    }

    override fun createWalletRepository(walletPrimalApiClient: PrimalApiClient): WalletRepository {
        return WalletRepositoryImpl(
            database = walletDatabase,
            walletPrimalApiClient = walletPrimalApiClient,
        )
    }
}
