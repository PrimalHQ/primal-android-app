package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.repository.factory.PrimalRepositoryFactory
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.FeedRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryCachingModule {

    @Provides
    fun provideFeedRepository(): FeedRepository = PrimalRepositoryFactory.createFeedRepository()

    @Provides
    fun provideEventRepository(): EventRepository = PrimalRepositoryFactory.createEventRepository()

    @Provides
    fun provideEventUriRepository(): EventUriRepository = PrimalRepositoryFactory.createEventUriRepository()

    @Provides
    fun provideEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository =
        PrimalRepositoryFactory.createEventInteractionRepository(primalPublisher)
}
