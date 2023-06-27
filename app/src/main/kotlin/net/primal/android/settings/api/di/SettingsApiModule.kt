package net.primal.android.settings.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.api.SettingsApiImpl
import net.primal.android.user.credentials.CredentialsStore

@Module
@InstallIn(SingletonComponent::class)
object SettingsApiModule {

    @Provides
    fun provideSettingsApi(
        socketClient: SocketClient,
        credentialsStore: CredentialsStore,
    ): SettingsApi = SettingsApiImpl(
        socketClient = socketClient,
        credentialsStore = credentialsStore,
    )

}
