package net.primal.android.settings.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.api.SettingsApiImpl

@Module
@InstallIn(SingletonComponent::class)
object SettingsApiModule {

    @Provides
    fun provideSettingsApi(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        relaysManager: RelaysManager,
        nostrNotary: NostrNotary,
    ): SettingsApi =
        SettingsApiImpl(
            primalApiClient = primalApiClient,
            relaysManager = relaysManager,
            nostrNotary = nostrNotary,
        )
}
