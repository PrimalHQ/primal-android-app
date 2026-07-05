package net.primal.data.repository.factory

import kotlin.experimental.ExperimentalObjCName
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.db.LocalDatabasePragmaConfig

typealias PrimalRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("PrimalRepositoryFactory")
object IosRepositoryFactory : CommonRepositoryFactory() {

    private val cachingDatabase: PrimalDatabase by lazy {
        LocalDatabaseFactory.createDatabase(
            databaseName = "primal_database.db",
            fallbackToDestructiveMigration = true,
            pragmaConfig = LocalDatabasePragmaConfig.CACHING,
        )
    }

    override fun resolveCachingDatabase(): PrimalDatabase = cachingDatabase
}
