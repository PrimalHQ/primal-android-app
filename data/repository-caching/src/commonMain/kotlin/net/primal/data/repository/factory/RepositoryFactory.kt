package net.primal.data.repository.factory

import net.primal.domain.repository.FeedRepository

internal interface RepositoryFactory {

    fun createFeedRepository(): FeedRepository
}
