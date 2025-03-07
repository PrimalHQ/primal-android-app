package net.primal.core.db

import net.primal.PrimalLib

object AppDatabaseProvider {
    fun getDatabase() = PrimalLib.getKoin().get<AppDatabase>()
}
