package net.primal.db

import net.primal.PrimalLib

object AppDatabaseProvider {
    fun getDatabase() = PrimalLib.getKoin().get<PrimalDatabase>()
}
