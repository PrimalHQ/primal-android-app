package net.primal.wallet.data.repository.factory

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalObjCName
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.spark.BreezApiKeyProvider
import net.primal.wallet.data.spark.BreezSdkStorageProvider
import net.primal.wallet.data.spark.IosBreezSdkStorageProvider

typealias WalletRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("WalletRepositoryFactory")
object IosRepositoryFactory : RepositoryFactory() {

    private val walletDatabase by lazy {
        LocalDatabaseFactory.createDatabase<WalletDatabase>(
            databaseName = "wallet_database.db",
            fallbackToDestructiveMigration = true,
            migrations = listOf(WalletDatabase.MIGRATION_2_3),
        )
    }

    fun init(
        enableDbEncryption: Boolean,
        enableLogs: Boolean,
        breezApiKey: String,
    ) {
        WalletDatabase.setEncryption(enableEncryption = enableDbEncryption)
        BreezApiKeyProvider.init(breezApiKey)
        if (enableLogs) {
            Napier.base(antilog = DebugAntilog())
        }
    }

    override fun resolveWalletDatabase(): WalletDatabase = walletDatabase

    override fun resolveBreezSdkStorageProvider(): BreezSdkStorageProvider = IosBreezSdkStorageProvider
}
