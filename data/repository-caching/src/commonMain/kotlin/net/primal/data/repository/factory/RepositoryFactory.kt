package net.primal.data.repository.factory

import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.EventInteractionRepository
import net.primal.domain.repository.EventRepository
import net.primal.domain.repository.EventUriRepository
import net.primal.domain.repository.FeedRepository
import net.primal.domain.repository.MutedUserRepository
import net.primal.domain.repository.ProfileRepository

internal interface RepositoryFactory {
    fun createFeedRepository(): FeedRepository
    fun createEventRepository(): EventRepository
    fun createEventUriRepository(): EventUriRepository
    fun createEventInteractionRepository(primalPublisher: PrimalPublisher): EventInteractionRepository
    fun createProfileRepository(primalPublisher: PrimalPublisher): ProfileRepository
    fun createMutedUserRepository(primalPublisher: PrimalPublisher): MutedUserRepository
}
