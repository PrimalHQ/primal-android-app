package net.primal.android.messages.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.messages.MessagesApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object MessagesApiModule {

    @Provides
    fun provideMessagesApi(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
    ): MessagesApi = PrimalApiServiceFactory.createMessagesApi(primalApiClient)

}
