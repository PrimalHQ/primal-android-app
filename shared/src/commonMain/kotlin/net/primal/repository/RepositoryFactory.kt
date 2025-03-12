package net.primal.repository

import net.primal.PrimalLib
import net.primal.repository.feed.FeedRepository

object RepositoryFactory {

    fun createFeedRepository() = PrimalLib.getKoin().get<FeedRepository>()
}
