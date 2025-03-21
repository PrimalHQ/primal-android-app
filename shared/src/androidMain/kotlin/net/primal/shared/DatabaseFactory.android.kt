package net.primal.shared

import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.PrimalDatabaseFactory

internal actual fun createPrimalDatabase(): PrimalDatabase {
    val appContext = PrimalInitializer.appContext
        ?: error("Please call first PrimalInitializer.init(ApplicationContext).")

    return PrimalDatabaseFactory.getDefaultDatabase(appContext)
}
