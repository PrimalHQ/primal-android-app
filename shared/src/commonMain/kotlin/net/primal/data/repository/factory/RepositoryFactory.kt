package net.primal.data.repository.factory

import net.primal.PrimalLib
import net.primal.domain.repository.FeedRepository

object RepositoryFactory {

    fun createFeedRepository() = PrimalLib.getKoin().get<FeedRepository>()
}
