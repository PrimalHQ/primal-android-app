package net.primal.android.notifications.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.api.NotificationsApiImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    fun provideNotificationsApi(
        @Named("Api") primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): NotificationsApi = NotificationsApiImpl(
        primalApiClient = primalApiClient,
        nostrNotary = nostrNotary,
    )
}
