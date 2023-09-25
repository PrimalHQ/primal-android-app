package net.primal.android.explore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.explore.api.ExploreApi
import net.primal.android.explore.api.ExploreApiImpl
import net.primal.android.networking.primal.PrimalApiClient
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object ExploreModule {
    @Provides
    fun provideExploreApi(
        @Named("Api") primalApiClient: PrimalApiClient,
    ): ExploreApi = ExploreApiImpl(
        primalApiClient = primalApiClient,
    )

}
