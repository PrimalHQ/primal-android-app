package net.primal.data.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.data.local.db.CachingDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.db.LocalDatabasePragmaConfig
import net.primal.shared.data.local.encryption.AndroidPlatformKeyStore

typealias PrimalRepositoryFactory = AndroidRepositoryFactory

object AndroidRepositoryFactory : CommonRepositoryFactory() {

    private var appContext: Context? = null

    private val cachingDatabase: CachingDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        LocalDatabaseFactory.deleteDatabases(
            context = appContext,
            names = CachingDatabase.OBSOLETE_FILE_NAMES,
        )
        LocalDatabaseFactory.createDatabase<CachingDatabase>(
            context = appContext,
            fallbackToDestructiveMigration = true,
            databaseName = "caching_database.db",
            pragmaConfig = LocalDatabasePragmaConfig.CACHING,
        )
    }

    fun init(context: Context) {
        this.appContext = context.applicationContext
        AndroidPlatformKeyStore.init(context)
        AppConfigInitializer.init(context)
    }

    override fun resolveCachingDatabase(): CachingDatabase = cachingDatabase
}
