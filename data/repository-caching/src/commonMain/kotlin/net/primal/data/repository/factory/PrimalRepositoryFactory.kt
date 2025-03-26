package net.primal.data.repository.factory

import net.primal.domain.repository.FeedRepository

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PrimalRepositoryFactory : RepositoryFactory {
    override fun createFeedRepository(): FeedRepository
}
