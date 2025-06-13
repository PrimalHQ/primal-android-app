package net.primal.data.repository.factory

import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

typealias PrimalRepositoryFactory = IosRepositoryFactory

object IosRepositoryFactory : CommonRepositoryFactory() {

    private val cachingDatabase: PrimalDatabase by lazy {
        LocalDatabaseFactory.createDatabase(databaseName = "primal_database.db")
    }

    override fun resolveCachingDatabase(): PrimalDatabase = cachingDatabase
}
