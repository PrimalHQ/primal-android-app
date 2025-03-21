package net.primal.android.explore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.explore.ExploreApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object ExploreModule {
    @Provides
    fun provideExploreApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ExploreApi =
        PrimalApiServiceFactory.createExploreApi(primalApiClient)
}
