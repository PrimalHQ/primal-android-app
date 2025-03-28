package net.primal.data.repository.factory

import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
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

internal interface RepositoryFactory {
    fun createArticleRepository(): ArticleRepository
    fun createArticleHighlightsRepository(primalPublisher: PrimalPublisher): HighlightRepository
    fun createChatRepository(messageCipher: MessageCipher, primalPublisher: PrimalPublisher): ChatRepository
    fun createFeedRepository(): FeedRepository
    fun createFeedsRepository(signatureHandler: NostrEventSignatureHandler): FeedsRepository
    fun createEventRepository(): EventRepository
    fun createEventUriRepository(): EventUriRepository
    fun createEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository
    fun createEventRelayHintsRepository(): EventRelayHintsRepository
    fun createExploreRepository(): ExploreRepository
    fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository
    fun createNotificationRepository(): NotificationRepository
    fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository
    fun createPublicBookmarksRepository(primalPublisher: PrimalPublisher): PublicBookmarksRepository
}
