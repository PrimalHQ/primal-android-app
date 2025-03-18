package net.primal.android.premium.manage.media.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.media.api.MediaManagementApi
import net.primal.android.premium.manage.media.api.MediaManagementApiImpl
import net.primal.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    fun provideMediaApi(
        @PrimalCacheApiClient primalCacheApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): MediaManagementApi =
        MediaManagementApiImpl(
            primalApiClient = primalCacheApiClient,
            nostrNotary = nostrNotary,
        )
}
