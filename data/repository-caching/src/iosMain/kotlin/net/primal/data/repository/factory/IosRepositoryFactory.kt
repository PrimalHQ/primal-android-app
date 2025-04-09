package net.primal.data.repository.factory

import net.primal.core.networking.factory.PrimalApiClientFactory
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
import net.primal.data.repository.messages.ChatRepositoryImpl
import net.primal.data.repository.messages.processors.MessagesProcessor
import net.primal.data.repository.mute.MutedUserRepositoryImpl
import net.primal.data.repository.notifications.NotificationRepositoryImpl
import net.primal.data.repository.profile.ProfileRepositoryImpl
import net.primal.domain.PrimalServerType
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.ArticleRepository
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

object IosRepositoryFactory : RepositoryFactory {

    private val dispatcherProvider = DispatcherProviderFactory.create()

    private val cachingPrimalApiClient = PrimalApiClientFactory.create(PrimalServerType.Caching)

    private val cachingDatabase by lazy { PrimalDatabaseFactory.getDefaultDatabase() }

    override fun createArticleRepository(): ArticleRepository {
        return ArticleRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            articlesApi = PrimalApiServiceFactory.createArticlesApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createArticleHighlightsRepository(primalPublisher: PrimalPublisher): HighlightRepository {
        return HighlightRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            primalPublisher = primalPublisher,
        )
    }

    override fun createChatRepository(messageCipher: MessageCipher, primalPublisher: PrimalPublisher): ChatRepository {
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

    override fun createFeedRepository(): FeedRepository {
        return FeedRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            feedApi = PrimalApiServiceFactory.createFeedApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createFeedsRepository(signatureHandler: NostrEventSignatureHandler): FeedsRepository {
        return FeedsRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            feedsApi = PrimalApiServiceFactory.createFeedsApi(cachingPrimalApiClient),
            database = cachingDatabase,
            signatureHandler = signatureHandler,
        )
    }

    override fun createEventRepository(): EventRepository {
        return EventRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            eventStatsApi = PrimalApiServiceFactory.createEventsApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createEventUriRepository(): EventUriRepository {
        return EventUriRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
        )
    }

    override fun createEventInteractionRepository(
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

    override fun createEventRelayHintsRepository(): EventRelayHintsRepository {
        return EventRelayHintsRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
        )
    }

    override fun createExploreRepository(): ExploreRepository {
        return ExploreRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            exploreApi = PrimalApiServiceFactory.createExploreApi(cachingPrimalApiClient),
            database = cachingDatabase,
        )
    }

    override fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository {
        return MutedUserRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            settingsApi = PrimalApiServiceFactory.createSettingsApi(cachingPrimalApiClient),
            primalPublisher = primalPublisher,
        )
    }

    override fun createNotificationRepository(): NotificationRepository {
        return NotificationRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            notificationsApi = PrimalApiServiceFactory.createNotificationsApi(cachingPrimalApiClient),
        )
    }

    override fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository {
        return ProfileRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            usersApi = PrimalApiServiceFactory.createUsersApi(cachingPrimalApiClient),
            wellKnownApi = PrimalApiServiceFactory.createUserWellKnownApi(),
            primalPublisher = primalPublisher,
        )
    }

    override fun createPublicBookmarksRepository(primalPublisher: PrimalPublisher): PublicBookmarksRepository {
        return PublicBookmarksRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = cachingDatabase,
            primalPublisher = primalPublisher,
            usersApi = PrimalApiServiceFactory.createUsersApi(cachingPrimalApiClient),
        )
    }

    override fun createUserDataCleanupRepository(): UserDataCleanupRepository {
        return UserDataCleanupRepositoryImpl(
            database = cachingDatabase,
        )
    }
}
