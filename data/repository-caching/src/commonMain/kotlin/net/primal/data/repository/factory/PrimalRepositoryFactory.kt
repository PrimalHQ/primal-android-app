package net.primal.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PrimalRepositoryFactory : RepositoryFactory {

    override fun createArticleRepository(cachingPrimalApiClient: PrimalApiClient): ArticleRepository

    override fun createArticleHighlightsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): HighlightRepository

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

    override fun createMutedUserRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): MutedUserRepository

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
