package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.repository.factory.PrimalRepositoryFactory
import net.primal.domain.repository.FeedRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryCachingModule {

    @Provides
    fun provideFeedRepository(): FeedRepository = PrimalRepositoryFactory.createFeedRepository()
}
