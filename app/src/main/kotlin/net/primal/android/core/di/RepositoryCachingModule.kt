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
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository
import net.primal.domain.repository.PublicBookmarksRepository

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

    @Provides
    fun provideProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository =
        PrimalRepositoryFactory.createProfileRepository(primalPublisher)

    @Provides
    fun provideMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository =
        PrimalRepositoryFactory.createMutedUserRepository(primalPublisher)

    @Provides
    fun providesPublicBookmarksRepository(primalPublisher: PrimalPublisher): PublicBookmarksRepository =
        PrimalRepositoryFactory.createPublicBookmarksRepository(primalPublisher)
}
