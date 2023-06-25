package net.primal.android.user.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.security.Encryption
import net.primal.android.serialization.CredentialsSerialization
import net.primal.android.serialization.UserAccountSerialization
import net.primal.android.user.api.UsersApi
import net.primal.android.user.api.UsersApiImpl
import net.primal.android.user.domain.Credential
import net.primal.android.user.domain.UserAccount
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
        encryption: Encryption,
    ): DataStore<List<Credential>> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile("credentials.json") },
        serializer = CredentialsSerialization(encryption = encryption),
    )

    @Provides
    @Singleton
    fun provideActiveAccountDataStore(
        @ApplicationContext context: Context,
        encryption: Encryption,
    ): DataStore<UserAccount> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile("active_account.json") },
        serializer = UserAccountSerialization(encryption = encryption),
    )

    @Provides
    fun provideUsersApi(
        socketClient: SocketClient,
    ): UsersApi = UsersApiImpl(
        socketClient = socketClient,
    )
}
