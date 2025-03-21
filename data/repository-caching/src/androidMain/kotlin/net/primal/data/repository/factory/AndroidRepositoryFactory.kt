package net.primal.data.repository.factory

import android.content.Context
import net.primal.core.config.store.AppConfigInitializer
import net.primal.core.networking.factory.PrimalApiClientFactory
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.PrimalDatabaseFactory
import net.primal.data.remote.factory.PrimalApiFactory
import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.domain.PrimalServerType
import net.primal.domain.repository.FeedRepository

object AndroidRepositoryFactory : RepositoryFactory {

    private var appContext: Context? = null

    private val cachingPrimalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching)

    private val cachingDatabase: PrimalDatabase by lazy {
        val appContext = appContext ?: error("You need to call init(ApplicationContext) first.")
        PrimalDatabaseFactory.getDefaultDatabase(appContext)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        AppConfigInitializer.init(context)
    }

    override fun createFeedRepository(): FeedRepository {
        return FeedRepositoryImpl(
            dispatcherProvider = DispatcherProviderFactory.create(),
            feedApi = PrimalApiFactory.createFeedApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }
}
