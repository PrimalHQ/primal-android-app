package net.primal.shared

import net.primal.data.local.db.PrimalDatabase

internal actual fun resolvePrimalDatabase(): PrimalDatabase {
    return PrimalDatabaseFactory.getDefaultDatabase()
}
