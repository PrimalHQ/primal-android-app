package net.primal.repository.di

import net.primal.repository.feed.FeedRepository
import net.primal.repository.feed.FeedRepositoryImpl
import org.koin.dsl.module

internal val repositoryModule = module {
    factory<FeedRepository> {
        FeedRepositoryImpl(
            dispatcherProvider = get(),
            feedApi = get(),
            database = get(),
        )
    }
}
