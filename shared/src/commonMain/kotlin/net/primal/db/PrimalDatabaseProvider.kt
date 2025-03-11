package net.primal.db

import net.primal.PrimalLib

object PrimalDatabaseProvider {
    fun getDatabase() = PrimalLib.getKoin().get<PrimalDatabase>()
}
