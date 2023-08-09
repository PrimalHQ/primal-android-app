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
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelayPool
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.security.Encryption
import net.primal.android.serialization.CredentialsSerialization
import net.primal.android.serialization.StringSerializer
import net.primal.android.serialization.UserAccountsSerialization
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
    fun provideCredentialsStore(
        @ApplicationContext context: Context,
        encryption: Encryption,
    ): DataStore<List<Credential>> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile("credentials.json") },
        serializer = CredentialsSerialization(encryption = encryption),
    )

    @Provides
    @Singleton
    fun provideUserAccountsStore(
        @ApplicationContext context: Context,
        encryption: Encryption,
    ): DataStore<List<UserAccount>> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile("accounts.json") },
        serializer = UserAccountsSerialization(encryption = encryption),
    )

    @Provides
    @Singleton
    @ActiveAccountDataStore
    fun provideActiveAccountDataStore(
        @ApplicationContext context: Context,
    ): DataStore<String> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile("active_account.txt") },
        serializer = StringSerializer(),
    )

    @Provides
    fun provideUsersApi(
        primalApiClient: PrimalApiClient,
        relayPool: RelayPool,
        nostrNotary: NostrNotary,
    ): UsersApi = UsersApiImpl(
        primalApiClient = primalApiClient,
        relayPool = relayPool,
        nostrNotary = nostrNotary,
    )
}
