package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.domain.events.EventRepository
import net.primal.domain.streams.StreamRepository
import net.primal.domain.usecase.UpdateStaleStreamDataUseCase

@Module
@InstallIn(SingletonComponent::class)
object StreamUseCasesModule {
    @Provides
    fun provideUpdateStaleStreamDataUseCase(
        streamRepository: StreamRepository,
        eventRepository: EventRepository,
    ): UpdateStaleStreamDataUseCase =
        UpdateStaleStreamDataUseCase(
            streamRepository = streamRepository,
            eventRepository = eventRepository,
        )
}
