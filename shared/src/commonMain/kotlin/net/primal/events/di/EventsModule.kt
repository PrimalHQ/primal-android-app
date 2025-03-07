package net.primal.events.di

import net.primal.events.repository.EventRepository
import net.primal.events.repository.EventRepositoryImpl
import org.koin.dsl.module

internal val eventsModule = module {
    factory<EventRepository> {
        EventRepositoryImpl(
            database = get(),
            dispatcherProvider = get(),
        )
    }
}
