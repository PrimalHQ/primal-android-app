package net.primal.wallet.data.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.encryption.AndroidPlatformKeyStore
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.spark.AndroidBreezSdkStorageProvider
import net.primal.wallet.data.spark.BreezApiKeyProvider
import net.primal.wallet.data.spark.BreezSdkStorageProvider

typealias WalletRepositoryFactory = AndroidRepositoryFactory

object AndroidRepositoryFactory : RepositoryFactory() {

    private var appContext: Context? = null

    private val walletDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        LocalDatabaseFactory.createDatabase<WalletDatabase>(
            context = appContext,
            fallbackToDestructiveMigration = true,
            databaseName = "wallet_database.db",
//            migrations = listOf(WalletDatabase.MIGRATION_2_3),
        )
    }

    fun init(
        context: Context,
        enableDbEncryption: Boolean,
        breezApiKey: String,
    ) {
        WalletDatabase.setEncryption(enableEncryption = enableDbEncryption)
        BreezApiKeyProvider.init(breezApiKey)
        this.appContext = context.applicationContext
        AndroidPlatformKeyStore.init(context)
        AppConfigInitializer.init(context)
        AndroidBreezSdkStorageProvider.init(context)
    }

    override fun resolveWalletDatabase(): WalletDatabase = walletDatabase

    override fun resolveBreezSdkStorageProvider(): BreezSdkStorageProvider = AndroidBreezSdkStorageProvider
}
