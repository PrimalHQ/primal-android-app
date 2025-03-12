package net.primal.api.feed.di

import net.primal.api.feed.FeedApi
import net.primal.api.feed.FeedApiImpl
import net.primal.networking.di.PrimalCacheApiClient
import org.koin.dsl.module

internal val primalFeedApiModule = module {
    factory<FeedApi> {
        FeedApiImpl(
            primalApiClient = get(PrimalCacheApiClient)
        )
    }
}
