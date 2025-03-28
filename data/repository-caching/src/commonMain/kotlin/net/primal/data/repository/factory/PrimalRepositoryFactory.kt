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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PrimalRepositoryFactory : RepositoryFactory {

    override fun createArticleRepository(): ArticleRepository

    override fun createArticleHighlightsRepository(primalPublisher: PrimalPublisher): HighlightRepository

    override fun createChatRepository(messageCipher: MessageCipher, primalPublisher: PrimalPublisher): ChatRepository

    override fun createFeedRepository(): FeedRepository

    override fun createFeedsRepository(signatureHandler: NostrEventSignatureHandler): FeedsRepository

    override fun createEventRepository(): EventRepository

    override fun createEventUriRepository(): EventUriRepository

    override fun createEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository

    override fun createEventRelayHintsRepository(): EventRelayHintsRepository

    override fun createExploreRepository(): ExploreRepository

    override fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository

    override fun createNotificationRepository(): NotificationRepository

    override fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository

    override fun createPublicBookmarksRepository(primalPublisher: PrimalPublisher): PublicBookmarksRepository
}
