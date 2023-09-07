package net.primal.android.settings.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.primal.PrimalClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.api.SettingsApiImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object SettingsApiModule {

    @Provides
    fun provideSettingsApi(
        @Named("Api") primalClient: PrimalClient,
        nostrNotary: NostrNotary,
    ): SettingsApi = SettingsApiImpl(
        primalClient = primalClient,
        nostrNotary = nostrNotary,
    )

}
