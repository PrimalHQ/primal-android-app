package net.primal.data.account.repository.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.data.account.local.db.AccountDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.encryption.AndroidPlatformKeyStore

typealias AccountRepositoryFactory = AndroidRepositoryFactory

object AndroidRepositoryFactory : RepositoryFactory() {

    private var appContext: Context? = null

    private val accountDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        LocalDatabaseFactory.createDatabase<AccountDatabase>(
            context = appContext,
            databaseName = "account_database.db",
            callback = AccountDatabase.provideDatabaseCallback(),
        )
    }

    fun init(context: Context, enableDbEncryption: Boolean) {
        AccountDatabase.setEncryption(enableEncryption = enableDbEncryption)
        this.appContext = context.applicationContext
        AndroidPlatformKeyStore.init(context)
        AppConfigInitializer.init(context)
    }

    override fun resolveAccountDatabase(): AccountDatabase = accountDatabase
}
