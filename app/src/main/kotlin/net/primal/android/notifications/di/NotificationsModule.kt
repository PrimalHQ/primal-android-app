package net.primal.android.notifications.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.api.NotificationsApiImpl
import net.primal.core.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    fun provideNotificationsApi(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): NotificationsApi =
        NotificationsApiImpl(
            primalApiClient = primalApiClient,
            nostrNotary = nostrNotary,
        )
}
