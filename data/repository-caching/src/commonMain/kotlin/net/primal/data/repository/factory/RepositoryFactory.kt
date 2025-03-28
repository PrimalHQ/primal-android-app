package net.primal.data.repository.factory

import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRelayHintsRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.FeedsRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository
import net.primal.domain.repository.PublicBookmarksRepository

internal interface RepositoryFactory {
    fun createFeedRepository(): FeedRepository
    fun createFeedsRepository(signatureHandler: NostrEventSignatureHandler): FeedsRepository
    fun createEventRepository(): EventRepository
    fun createEventUriRepository(): EventUriRepository
    fun createEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository
    fun createEventRelayHintsRepository(): EventRelayHintsRepository
    fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository
    fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository
    fun createPublicBookmarksRepository(primalPublisher: PrimalPublisher): PublicBookmarksRepository
}
