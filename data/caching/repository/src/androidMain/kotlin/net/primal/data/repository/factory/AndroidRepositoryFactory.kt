package net.primal.data.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.encryption.AndroidPlatformKeyStore

typealias PrimalRepositoryFactory = AndroidRepositoryFactory

object AndroidRepositoryFactory : CommonRepositoryFactory() {

    private var appContext: Context? = null

    private val cachingDatabase: PrimalDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        LocalDatabaseFactory.createDatabase<PrimalDatabase>(
            context = appContext,
            databaseName = "primal_database.db",
        )
    }

    fun init(context: Context) {
        this.appContext = context.applicationContext
        AndroidPlatformKeyStore.init(context)
        AppConfigInitializer.init(context)
    }

    override fun resolveCachingDatabase(): PrimalDatabase = cachingDatabase
}
