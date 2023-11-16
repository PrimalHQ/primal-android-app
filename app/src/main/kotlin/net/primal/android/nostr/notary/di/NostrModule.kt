package net.primal.android.nostr.notary.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.credentials.CredentialsStore

@Module
@InstallIn(SingletonComponent::class)
object NostrModule {

    @Provides
    fun provideNostrNotary(credentialsStore: CredentialsStore): NostrNotary =
        NostrNotary(
            credentialsStore = credentialsStore,
        )
}
