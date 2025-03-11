package net.primal.api.feeds.di

import net.primal.api.feeds.FeedApi
import net.primal.api.feeds.FeedApiImpl
import net.primal.networking.di.PrimalCacheApiClient
import org.koin.dsl.module

internal val primalFeedApiModule = module {
    factory<FeedApi> {
        FeedApiImpl(
            primalApiClient = get(PrimalCacheApiClient)
        )
    }
}
