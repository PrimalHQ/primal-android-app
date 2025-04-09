package net.primal.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
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

internal interface RepositoryFactory {

    fun createArticleRepository(cachingPrimalApiClient: PrimalApiClient): ArticleRepository

    fun createArticleHighlightsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): HighlightRepository

    fun createCachingImportRepository(): CachingImportRepository

    fun createChatRepository(
        cachingPrimalApiClient: PrimalApiClient,
        messageCipher: MessageCipher,
        primalPublisher: PrimalPublisher,
    ): ChatRepository

    fun createFeedRepository(cachingPrimalApiClient: PrimalApiClient): FeedRepository

    fun createFeedsRepository(
        cachingPrimalApiClient: PrimalApiClient,
        signatureHandler: NostrEventSignatureHandler,
    ): FeedsRepository

    fun createEventRepository(cachingPrimalApiClient: PrimalApiClient): EventRepository

    fun createEventUriRepository(cachingPrimalApiClient: PrimalApiClient): EventUriRepository

    fun createEventInteractionRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
        nostrZapperFactory: NostrZapperFactory,
    ): EventInteractionRepository

    fun createEventRelayHintsRepository(cachingPrimalApiClient: PrimalApiClient): EventRelayHintsRepository

    fun createExploreRepository(cachingPrimalApiClient: PrimalApiClient): ExploreRepository

    fun createMutedUserRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): MutedUserRepository

    fun createNotificationRepository(cachingPrimalApiClient: PrimalApiClient): NotificationRepository

    fun createProfileRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): ProfileRepository

    fun createPublicBookmarksRepository(
        cachingPrimalApiClient: PrimalApiClient,
        primalPublisher: PrimalPublisher,
    ): PublicBookmarksRepository

    fun createUserDataCleanupRepository(cachingPrimalApiClient: PrimalApiClient): UserDataCleanupRepository
}
