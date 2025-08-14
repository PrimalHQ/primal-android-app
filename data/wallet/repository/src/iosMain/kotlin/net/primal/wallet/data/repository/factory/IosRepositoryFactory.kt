package net.primal.wallet.data.repository.factory

import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.wallet.data.local.db.WalletDatabase

typealias WalletRepositoryFactory = IosRepositoryFactory

object IosRepositoryFactory : RepositoryFactory() {

    private val walletDatabase by lazy {
        LocalDatabaseFactory.createDatabase<WalletDatabase>(databaseName = "wallet_database.db")
    }

    fun init(enableDbEncryption: Boolean) {
        WalletDatabase.setEncryption(enableEncryption = enableDbEncryption)
    }

    override fun resolveWalletDatabase(): WalletDatabase = walletDatabase
}
