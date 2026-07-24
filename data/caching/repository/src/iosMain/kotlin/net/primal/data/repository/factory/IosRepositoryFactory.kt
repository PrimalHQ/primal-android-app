package net.primal.data.repository.factory

import kotlin.experimental.ExperimentalObjCName
import net.primal.data.local.db.CachingDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory
import net.primal.shared.data.local.db.LocalDatabasePragmaConfig

typealias PrimalRepositoryFactory = IosRepositoryFactory

@OptIn(ExperimentalObjCName::class)
@ObjCName("PrimalRepositoryFactory")
object IosRepositoryFactory : CommonRepositoryFactory() {

    private val cachingDatabase: CachingDatabase by lazy {
        LocalDatabaseFactory.deleteDatabases(CachingDatabase.OBSOLETE_FILE_NAMES)
        LocalDatabaseFactory.deleteDatabaseIfOversized(
            databaseName = "caching_database.db",
            maxSizeBytes = CachingDatabase.MAX_DATABASE_SIZE_BYTES,
        )
        LocalDatabaseFactory.createDatabase(
            databaseName = "caching_database.db",
            fallbackToDestructiveMigration = true,
            pragmaConfig = LocalDatabasePragmaConfig.CACHING,
        )
    }

    override fun resolveCachingDatabase(): CachingDatabase = cachingDatabase
}
