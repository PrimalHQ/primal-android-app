package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostrconnect.handler.Nip46EventsHandlerImpl
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.nips.encryption.service.factory.NipsEncryptionFactory
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.data.account.repository.service.AndroidAccountServiceFactory
import net.primal.data.account.repository.service.LocalSignerService
import net.primal.data.remote.factory.PrimalApiServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

@Module
@InstallIn(SingletonComponent::class)
object AccountRepositoriesModule {
    @Provides
    fun provideConnectionRepository(): ConnectionRepository = AccountRepositoryFactory.createConnectionRepository()

    @Provides
    fun provideSessionRepository(): SessionRepository = AccountRepositoryFactory.createSessionRepository()

    @Provides
    fun provideSessionEventRepository(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        dispatchers: DispatcherProvider,
    ): SessionEventRepository =
        AccountRepositoryFactory.createSessionEventRepository(
            nip46EventsHandler = Nip46EventsHandlerImpl(
                eventStatsApi = PrimalApiServiceFactory.createEventsApi(primalApiClient = primalApiClient),
                dispatchers = dispatchers,
            ),
        )

    @Provides
    fun providePermissionsRepository(): PermissionsRepository = AccountRepositoryFactory.createPermissionsRepository()

    @Provides
    fun provideNostrEncryptionService(): NostrEncryptionService = NipsEncryptionFactory.createNostrEncryptionService()

    @Provides
    fun provideLocalAppRepository(): LocalAppRepository = AccountRepositoryFactory.createLocalAppRepository()

    @Provides
    fun provideLocalSignerService(
        localAppRepository: LocalAppRepository,
        permissionsRepository: PermissionsRepository,
        nostrEncryptionHandler: NostrEncryptionHandler,
        eventSignatureHandler: NostrEventSignatureHandler,
    ): LocalSignerService =
        AndroidAccountServiceFactory.createLocalSignerService(
            localAppRepository = localAppRepository,
            permissionsRepository = permissionsRepository,
            nostrEncryptionHandler = nostrEncryptionHandler,
            eventSignatureHandler = eventSignatureHandler,
        )

    @Provides
    fun provideSignerConnectionInitializer(
        connectionRepository: ConnectionRepository,
        sessionRepository: SessionRepository,
    ): SignerConnectionInitializer =
        AccountRepositoryFactory.createSignerConnectionInitializer(
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
        )
}
