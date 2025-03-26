package net.primal.android.nostr.notary.di

import android.content.ContentResolver
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
    fun provideNostrNotary(contentResolver: ContentResolver, credentialsStore: CredentialsStore): NostrNotary =
        NostrNotary(
            contentResolver = contentResolver,
            credentialsStore = credentialsStore,
        )
}
