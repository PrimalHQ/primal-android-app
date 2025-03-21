package net.primal.shared

import net.primal.core.networking.factory.PrimalApiClientFactory
import net.primal.data.remote.factory.PrimalApiFactory
import net.primal.data.repository.factory.RepositoryFactory
import net.primal.domain.PrimalServerType
import net.primal.domain.repository.FeedRepository

object RepositoryModule {

    private val cachingPrimalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching)

    private val cachingDatabase = createPrimalDatabase()

    val feedRepository: FeedRepository by lazy {
        RepositoryFactory.createFeedRepository(
            feedApi = PrimalApiFactory.createFeedsApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

}
