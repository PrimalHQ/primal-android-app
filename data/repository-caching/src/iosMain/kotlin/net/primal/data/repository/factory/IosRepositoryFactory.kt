package net.primal.data.repository.factory

import net.primal.core.networking.factory.PrimalApiClientFactory
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.data.local.db.PrimalDatabaseFactory
import net.primal.data.remote.factory.PrimalApiServiceFactory
import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.domain.PrimalServerType
import net.primal.domain.repository.FeedRepository

object IosRepositoryFactory : RepositoryFactory {

    private val cachingPrimalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching)

    private val cachingDatabase by lazy {
        PrimalDatabaseFactory.getDefaultDatabase()
    }

    override fun createFeedRepository(): FeedRepository {
        return FeedRepositoryImpl(
            dispatcherProvider = DispatcherProviderFactory.create(),
            feedApi = PrimalApiServiceFactory.createFeedApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

}
