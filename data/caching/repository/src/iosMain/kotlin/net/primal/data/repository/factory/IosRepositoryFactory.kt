package net.primal.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.data.local.db.PrimalDatabaseFactory
import net.primal.data.remote.factory.PrimalApiServiceFactory
import net.primal.data.repository.UserDataCleanupRepositoryImpl
import net.primal.data.repository.articles.ArticleRepositoryImpl
import net.primal.data.repository.articles.HighlightRepositoryImpl
import net.primal.data.repository.bookmarks.PublicBookmarksRepositoryImpl
import net.primal.data.repository.events.EventInteractionRepositoryImpl
import net.primal.data.repository.events.EventRelayHintsRepositoryImpl
import net.primal.data.repository.events.EventRepositoryImpl
import net.primal.data.repository.events.EventUriRepositoryImpl
import net.primal.data.repository.explore.ExploreRepositoryImpl
import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.data.repository.feeds.FeedsRepositoryImpl
import net.primal.data.repository.importer.CachingImportRepositoryImpl
import net.primal.data.repository.messages.ChatRepositoryImpl
import net.primal.data.repository.messages.processors.MessagesProcessor
import net.primal.data.repository.mute.MutedItemRepositoryImpl
import net.primal.data.repository.notifications.NotificationRepositoryImpl
import net.primal.data.repository.profile.ProfileRepositoryImpl
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.events.EventRepository
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.global.CachingImportRepository
import net.primal.domain.links.EventUriRepository
import net.primal.domain.messages.ChatRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.notifications.NotificationRepository
import net.primal.domain.posts.FeedRepository
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.reads.ArticleRepository
import net.primal.domain.reads.HighlightRepository
import net.primal.domain.user.UserDataCleanupRepository

object IosRepositoryFactory : RepositoryFactory {

    private val dispatcherProvider = DispatcherProviderFactory.create()

    private val cachingDatabase by lazy { PrimalDatabaseFactory.getDefaultDatabase() }

    override fun createArticleRepository(cachingPrimalApiClient: PrimalApiClient): ArticleRepository {
        return ArticleRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            articlesApi = PrimalApiServiceFactory.createArticlesApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createArticleHighlightsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): HighlightRepository {
        return HighlightRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            primalPublisher = primalPublisher,
        )
    }

    override fun createCachingImportRepository(): CachingImportRepository {
        return CachingImportRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
        )
    }

    override fun createChatRepository(
        cachingPrimalApiClient: PrimalApiClient,
        messageCipher: MessageCipher,
        primalPublisher: PrimalPublisher,
    ): ChatRepository {
        return ChatRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            messageCipher = messageCipher,
            messagesApi = PrimalApiServiceFactory.createMessagesApi(cachingPrimalApiClient),
            messagesProcessor = MessagesProcessor(
                database = cachingDatabase,
                feedApi = PrimalApiServiceFactory.createFeedApi(cachingPrimalApiClient),
                usersApi = PrimalApiServiceFactory.createUsersApi(cachingPrimalApiClient),
                messageCipher = messageCipher,
            ),
            primalPublisher = primalPublisher,
        )
    }

    override fun createFeedRepository(cachingPrimalApiClient: PrimalApiClient): FeedRepository {
        return FeedRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            feedApi = PrimalApiServiceFactory.createFeedApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createFeedsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        signatureHandler: NostrEventSignatureHandler,
    ): FeedsRepository {
        return FeedsRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            feedsApi = PrimalApiServiceFactory.createFeedsApi(cachingPrimalApiClient),
            database = cachingDatabase,
            signatureHandler = signatureHandler,
        )
    }

    override fun createEventRepository(cachingPrimalApiClient: PrimalApiClient): EventRepository {
        return EventRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            eventStatsApi = PrimalApiServiceFactory.createEventsApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createEventUriRepository(cachingPrimalApiClient: PrimalApiClient): EventUriRepository {
        return EventUriRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
        )
    }

    override fun createEventInteractionRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
        nostrZapperFactory: NostrZapperFactory,
    ): EventInteractionRepository {
        return EventInteractionRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalPublisher = primalPublisher,
            nostrZapperFactory = nostrZapperFactory,
            database = cachingDatabase,
        )
    }

    override fun createEventRelayHintsRepository(cachingPrimalApiClient: PrimalApiClient): EventRelayHintsRepository {
        return EventRelayHintsRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
        )
    }

    override fun createExploreRepository(cachingPrimalApiClient: PrimalApiClient): ExploreRepository {
        return ExploreRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            exploreApi = PrimalApiServiceFactory.createExploreApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createMutedItemRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): MutedItemRepository {
        return MutedItemRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            settingsApi = PrimalApiServiceFactory.createSettingsApi(cachingPrimalApiClient),
            primalPublisher = primalPublisher,
        )
    }

    override fun createNotificationRepository(cachingPrimalApiClient: PrimalApiClient): NotificationRepository {
        return NotificationRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            notificationsApi = PrimalApiServiceFactory.createNotificationsApi(cachingPrimalApiClient),
        )
    }

    override fun createProfileRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): ProfileRepository {
        return ProfileRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            usersApi = PrimalApiServiceFactory.createUsersApi(cachingPrimalApiClient),
            wellKnownApi = PrimalApiServiceFactory.createUserWellKnownApi(),
            primalPublisher = primalPublisher,
        )
    }

    override fun createPublicBookmarksRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): PublicBookmarksRepository {
        return PublicBookmarksRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            primalPublisher = primalPublisher,
            usersApi = PrimalApiServiceFactory.createUsersApi(cachingPrimalApiClient),
        )
    }

    override fun createUserDataCleanupRepository(cachingPrimalApiClient: PrimalApiClient): UserDataCleanupRepository {
        return UserDataCleanupRepositoryImpl(
            database = cachingDatabase,
        )
    }
}
