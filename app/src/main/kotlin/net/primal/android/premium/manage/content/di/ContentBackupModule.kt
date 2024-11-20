package net.primal.android.premium.manage.content.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.content.api.BroadcastApi
import net.primal.android.premium.manage.content.api.BroadcastApiImpl

@Module
@InstallIn(SingletonComponent::class)
object ContentBackupModule {

    @Provides
    fun provideBroadcastApi(
        @PrimalCacheApiClient primalCacheApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): BroadcastApi =
        BroadcastApiImpl(
            primalCacheApiClient = primalCacheApiClient,
            nostrNotary = nostrNotary,
        )
}
