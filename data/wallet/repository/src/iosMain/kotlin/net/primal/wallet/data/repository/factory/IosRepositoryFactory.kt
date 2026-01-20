package net.primal.wallet.data.repository.factory

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalObjCName
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.wallet.data.local.db.WalletDatabase

typealias WalletRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("WalletRepositoryFactory")
object IosRepositoryFactory : RepositoryFactory() {

    private val walletDatabase by lazy {
        LocalDatabaseFactory.createDatabase<WalletDatabase>(
            databaseName = "wallet_database.db",
            fallbackToDestructiveMigration = true,
        )
    }

    fun init(enableDbEncryption: Boolean, enableLogs: Boolean) {
        WalletDatabase.setEncryption(enableEncryption = enableDbEncryption)
        if (enableLogs) {
            Napier.base(antilog = DebugAntilog())
        }
    }

    override fun resolveWalletDatabase(): WalletDatabase = walletDatabase
}
