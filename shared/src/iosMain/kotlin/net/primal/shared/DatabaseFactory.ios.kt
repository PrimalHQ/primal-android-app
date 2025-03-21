package net.primal.shared

import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.PrimalDatabaseFactory

internal actual fun createPrimalDatabase(): PrimalDatabase {
    return PrimalDatabaseFactory.getDefaultDatabase()
}
