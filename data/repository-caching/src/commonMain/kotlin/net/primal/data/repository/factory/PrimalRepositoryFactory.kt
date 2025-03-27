package net.primal.data.repository.factory

import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PrimalRepositoryFactory : RepositoryFactory {
    override fun createFeedRepository(): FeedRepository
    override fun createEventRepository(): EventRepository
    override fun createEventUriRepository(): EventUriRepository
    override fun createEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository
    override fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository
    override fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository
}
