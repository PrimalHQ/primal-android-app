package net.primal.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PrimalRepositoryFactory : RepositoryFactory {

    override fun createArticleRepository(cachingPrimalApiClient: PrimalApiClient): ArticleRepository

    override fun createArticleHighlightsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): HighlightRepository

    override fun createCachingImportRepository(): CachingImportRepository

    override fun createChatRepository(
        cachingPrimalApiClient: PrimalApiClient,
        messageCipher: MessageCipher,
        primalPublisher: PrimalPublisher,
    ): ChatRepository

    override fun createFeedRepository(cachingPrimalApiClient: PrimalApiClient): FeedRepository

    override fun createFeedsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        signatureHandler: NostrEventSignatureHandler,
    ): FeedsRepository

    override fun createEventRepository(cachingPrimalApiClient: PrimalApiClient): EventRepository

    override fun createEventUriRepository(cachingPrimalApiClient: PrimalApiClient): EventUriRepository

    override fun createEventInteractionRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
        nostrZapperFactory: NostrZapperFactory,
    ): EventInteractionRepository

    override fun createEventRelayHintsRepository(cachingPrimalApiClient: PrimalApiClient): EventRelayHintsRepository

    override fun createExploreRepository(cachingPrimalApiClient: PrimalApiClient): ExploreRepository

    override fun createMutedItemRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): MutedItemRepository

    override fun createNotificationRepository(cachingPrimalApiClient: PrimalApiClient): NotificationRepository

    override fun createProfileRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): ProfileRepository

    override fun createPublicBookmarksRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): PublicBookmarksRepository

    override fun createUserDataCleanupRepository(cachingPrimalApiClient: PrimalApiClient): UserDataCleanupRepository
}
