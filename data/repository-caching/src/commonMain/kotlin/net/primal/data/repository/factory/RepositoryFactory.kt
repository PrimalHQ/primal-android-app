package net.primal.data.repository.factory

import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.domain.repository.FeedRepository

object RepositoryFactory {

    fun createFeedRepository(
        feedApi: FeedApi,
        database: PrimalDatabase,
    ): FeedRepository {
        return FeedRepositoryImpl(
            dispatcherProvider = DispatcherProviderFactory.create(),
            feedApi = feedApi,
            database = database,
        )
    }

}
