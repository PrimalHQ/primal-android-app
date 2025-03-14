package net.primal.data.local.db

import net.primal.PrimalLib

object PrimalDatabaseProvider {
    fun getDatabase() = PrimalLib.getKoin().get<net.primal.data.local.db.PrimalDatabase>()
}
