package net.primal.android.articles.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.factory.PrimalApiFactory

@Module
@InstallIn(SingletonComponent::class)
object ArticlesApiModule {
    @Provides
    fun provideArticlesApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ArticlesApi =
        PrimalApiFactory.createArticlesApi(primalApiClient)
}
