package net.primal.android.notifications.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.notifications.NotificationsApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    fun provideNotificationsApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): NotificationsApi =
        PrimalApiServiceFactory.createNotificationsApi(primalApiClient)
}
