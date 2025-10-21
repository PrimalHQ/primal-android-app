package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.data.account.repository.service.factory.AccountServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.NostrEncryptionService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

@Module
@InstallIn(SingletonComponent::class)
object AccountRepositoriesModule {
    @Provides
    fun provideConnectionRepository(): ConnectionRepository =
        AccountRepositoryFactory.createConnectionRepository()

    @Provides
    fun provideNostrEncryptionService(): NostrEncryptionService =
        AccountServiceFactory.createNostrEncryptionService()

    @Provides
    fun provideRemoteSignerServiceFactory(
        eventSignatureHandler: NostrEventSignatureHandler,
        nostrEncryptionHandler: NostrEncryptionHandler,
        connectionRepository: ConnectionRepository,
        dispatcherProvider: DispatcherProvider,
    ): RemoteSignerServiceFactory =
        RemoteSignerServiceFactory(
            eventSignatureHandler = eventSignatureHandler,
            nostrEncryptionHandler = nostrEncryptionHandler,
            connectionRepository = connectionRepository,
            dispatcherProvider = dispatcherProvider,
        )

    @Provides
    fun provideConnectionInitializerFactory(
        connectionRepository: ConnectionRepository,
    ): SignerConnectionInitializerFactory =
        SignerConnectionInitializerFactory(
            connectionRepository = connectionRepository,
        )
}
