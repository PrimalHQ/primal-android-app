package net.primal.data.repository.di

import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.domain.repository.FeedRepository
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
