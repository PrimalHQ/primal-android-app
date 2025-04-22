package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.factory.PublisherFactory
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.publisher.NostrEventImporter
import net.primal.domain.publisher.PrimalPublisher

@Module
@InstallIn(SingletonComponent::class)
object NostrModule {

    @Provides
    fun provideNostrEventImporter(@PrimalCacheApiClient primalApiClient: PrimalApiClient): NostrEventImporter =
        PublisherFactory.createNostrEventImporter(primalApiClient)

    @Provides
    fun providePrimalPublisher(nostrPublisher: NostrPublisher): PrimalPublisher = nostrPublisher

    @Provides
    fun provideNostrEventSignatureHandler(nostrNotary: NostrNotary): NostrEventSignatureHandler = nostrNotary
}
