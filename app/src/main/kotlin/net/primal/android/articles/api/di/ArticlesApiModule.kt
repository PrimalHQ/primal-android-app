package net.primal.android.articles.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.articles.api.ArticlesApi
import net.primal.android.articles.api.ArticlesApiImpl
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object ArticlesApiModule {
    @Provides
    fun provideArticlesApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ArticlesApi =
        ArticlesApiImpl(
            primalApiClient = primalApiClient,
        )
}
