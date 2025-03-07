package net.primal.events.repository

import net.primal.PrimalLib

object EventRepositoryFactory {
    fun create(): EventRepository = PrimalLib.getKoin().get()
}
