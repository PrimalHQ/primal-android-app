package net.primal.data.account.repository.repository.factory

import kotlin.experimental.ExperimentalObjCName
import net.primal.data.account.local.db.AccountDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

typealias AccountRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("AccountRepositoryFactory")
object IosRepositoryFactory : RepositoryFactory() {

    private val accountDatabase by lazy {
        LocalDatabaseFactory.createDatabase<AccountDatabase>(databaseName = "account_database.db")
    }

    fun init(enableDbEncryption: Boolean) {
        AccountDatabase.setEncryption(enableEncryption = enableDbEncryption)
    }

    override fun resolveAccountDatabase(): AccountDatabase = accountDatabase
}
