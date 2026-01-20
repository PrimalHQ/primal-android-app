package net.primal.data.account.repository.repository.factory

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalObjCName
import net.primal.data.account.local.db.AccountDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

typealias AccountRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("AccountRepositoryFactory")
object IosRepositoryFactory : RepositoryFactory() {

    private val accountDatabase by lazy {
        LocalDatabaseFactory.createDatabase<AccountDatabase>(
            databaseName = "account_database.db",
            fallbackToDestructiveMigration = true,
            callback = AccountDatabase.provideDatabaseCallback(),
        )
    }

    fun init(enableDbEncryption: Boolean, enableLogs: Boolean) {
        AccountDatabase.setEncryption(enableEncryption = enableDbEncryption)
        if (enableLogs) {
            Napier.base(antilog = DebugAntilog())
        }
    }

    override fun resolveAccountDatabase(): AccountDatabase = accountDatabase
}

actual fun provideAccountDatabase(): AccountDatabase = IosRepositoryFactory.resolveAccountDatabase()
