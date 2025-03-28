package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.data.repository.factory.PrimalRepositoryFactory
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.ArticleRepository
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRelayHintsRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.FeedsRepository
import net.primal.domain.repository.HighlightRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository
import net.primal.domain.repository.PublicBookmarksRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryCachingModule {

    @Provides
    fun provideFeedRepository(): FeedRepository = PrimalRepositoryFactory.createFeedRepository()

    @Provides
    fun provideFeedsRepository(nostrNotary: NostrNotary): FeedsRepository =
        PrimalRepositoryFactory.createFeedsRepository(signatureHandler = nostrNotary)

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

    @Provides
    fun providesEventRelayHintsRepository(): EventRelayHintsRepository =
        PrimalRepositoryFactory.createEventRelayHintsRepository()

    @Provides
    fun providesArticleRepository(): ArticleRepository = PrimalRepositoryFactory.createArticleRepository()

    @Provides
    fun providesHighlightsRepository(primalPublisher: PrimalPublisher): HighlightRepository =
        PrimalRepositoryFactory.createArticleHighlightsRepository(primalPublisher)
}
