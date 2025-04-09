package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.repository.factory.PrimalRepositoryFactory
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.ArticleRepository
import net.primal.domain.repository.CachingImportRepository
import net.primal.domain.repository.ChatRepository
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRelayHintsRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.ExploreRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.FeedsRepository
import net.primal.domain.repository.HighlightRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.NotificationRepository
import net.primal.domain.repository.ProfileRepository
import net.primal.domain.repository.PublicBookmarksRepository
import net.primal.domain.repository.UserDataCleanupRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryCachingModule {

    @Provides
    fun providesArticleRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ArticleRepository {
        return PrimalRepositoryFactory.createArticleRepository(cachingPrimalApiClient = primalApiClient)
    }

    @Provides
    fun providesArticleHighlightsRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): HighlightRepository {
        return PrimalRepositoryFactory.createArticleHighlightsRepository(
            cachingPrimalApiClient = primalApiClient,
            primalPublisher = primalPublisher,
        )
    }

    @Provides
    fun providesCachingImporterRepository(): CachingImportRepository {
        return PrimalRepositoryFactory.createCachingImportRepository()
    }

    @Provides
    fun provideChatRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        messageCipher: MessageCipher,
        primalPublisher: PrimalPublisher,
    ): ChatRepository =
        PrimalRepositoryFactory.createChatRepository(
            cachingPrimalApiClient = primalApiClient,
            messageCipher = messageCipher,
            primalPublisher = primalPublisher,
        )

    @Provides
    fun provideFeedRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): FeedRepository =
        PrimalRepositoryFactory.createFeedRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun provideFeedsRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): FeedsRepository =
        PrimalRepositoryFactory.createFeedsRepository(
            cachingPrimalApiClient = primalApiClient,
            signatureHandler = nostrNotary,
        )

    @Provides
    fun provideEventRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): EventRepository =
        PrimalRepositoryFactory.createEventRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun provideEventUriRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): EventUriRepository =
        PrimalRepositoryFactory.createEventUriRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun provideEventInteractionRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
        nostrZapperFactory: NostrZapperFactory,
    ): EventInteractionRepository =
        PrimalRepositoryFactory.createEventInteractionRepository(
            cachingPrimalApiClient = primalApiClient,
            primalPublisher = primalPublisher,
            nostrZapperFactory = nostrZapperFactory,
        )

    @Provides
    fun provideExploreRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ExploreRepository =
        PrimalRepositoryFactory.createExploreRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun provideProfileRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): ProfileRepository =
        PrimalRepositoryFactory.createProfileRepository(cachingPrimalApiClient = primalApiClient, primalPublisher)

    @Provides
    fun provideMutedUserRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): MutedUserRepository =
        PrimalRepositoryFactory.createMutedUserRepository(cachingPrimalApiClient = primalApiClient, primalPublisher)

    @Provides
    fun provideNotificationRepository(@PrimalCacheApiClient primalApiClient: PrimalApiClient): NotificationRepository =
        PrimalRepositoryFactory.createNotificationRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun providesPublicBookmarksRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): PublicBookmarksRepository =
        PrimalRepositoryFactory.createPublicBookmarksRepository(
            cachingPrimalApiClient = primalApiClient,
            primalPublisher,
        )

    @Provides
    fun providesEventRelayHintsRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
    ): EventRelayHintsRepository =
        PrimalRepositoryFactory.createEventRelayHintsRepository(cachingPrimalApiClient = primalApiClient)

    @Provides
    fun provideUserDataCleanupRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
    ): UserDataCleanupRepository =
        PrimalRepositoryFactory.createUserDataCleanupRepository(cachingPrimalApiClient = primalApiClient)
}
