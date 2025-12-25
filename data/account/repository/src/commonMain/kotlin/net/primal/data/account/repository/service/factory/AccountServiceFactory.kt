package net.primal.data.account.repository.service.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.remote.api.WellKnownApi
import net.primal.data.account.remote.api.createWellKnownApi
import net.primal.data.account.remote.method.parser.RemoteSignerMethodParser
import net.primal.data.account.repository.builder.LocalSignerMethodResponseBuilder
import net.primal.data.account.repository.builder.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.repository.factory.provideAccountDatabase
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalSessionEventRepository
import net.primal.data.account.repository.repository.internal.InternalSessionRepository
import net.primal.data.account.repository.service.LocalSignerServiceImpl
import net.primal.data.account.repository.service.RemoteSignerServiceImpl
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.LocalSignerService
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

object AccountServiceFactory {
    private val httpClient = HttpClientFactory.createHttpClientWithDefaultConfig {
        install(HttpCache)
    }

    private val wellKnownApi: WellKnownApi by lazy {
        Ktorfit.Builder()
            .baseUrl("https://primal.net/")
            .httpClient(client = httpClient)
            .build()
            .createWellKnownApi()
    }

    fun createRemoteSignerService(
        signerKeyPair: NostrKeyPair,
        eventSignatureHandler: NostrEventSignatureHandler,
        nostrEncryptionService: NostrEncryptionService,
        nostrEncryptionHandler: NostrEncryptionHandler,
        connectionRepository: ConnectionRepository,
        sessionRepository: SessionRepository,
        sessionInactivityTimeoutInMinutes: Long = 0,
    ): RemoteSignerService {
        val dispatchers = createDispatcherProvider()
        val accountDatabase = provideAccountDatabase()
        return RemoteSignerServiceImpl(
            signerKeyPair = signerKeyPair,
            sessionInactivityTimeoutInMinutes = sessionInactivityTimeoutInMinutes,
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            nostrRelayManager = NostrRelayManager(
                dispatcherProvider = dispatchers,
                signerKeyPair = signerKeyPair,
                nostrEncryptionService = nostrEncryptionService,
            ),
            remoteSignerMethodResponseBuilder = RemoteSignerMethodResponseBuilder(
                nostrEventSignatureHandler = eventSignatureHandler,
                nostrEncryptionHandler = nostrEncryptionHandler,
                connectionRepository = connectionRepository,
            ),
            internalSessionEventRepository = InternalSessionEventRepository(
                dispatchers = dispatchers,
                accountDatabase = accountDatabase,
            ),
            internalSessionRepository = InternalSessionRepository(
                dispatchers = dispatchers,
                database = accountDatabase,
            ),
            remoteSignerMethodParser = RemoteSignerMethodParser(
                nostrEncryptionService = nostrEncryptionService,
            ),
        )
    }

    fun createLocalSignerService(
        localAppRepository: LocalAppRepository,
        permissionsRepository: PermissionsRepository,
        nostrEncryptionHandler: NostrEncryptionHandler,
        eventSignatureHandler: NostrEventSignatureHandler,
    ): LocalSignerService {
        val dispatchers = createDispatcherProvider()
        val accountDatabase = provideAccountDatabase()
        return LocalSignerServiceImpl(
            localAppRepository = localAppRepository,
            permissionsRepository = permissionsRepository,
            localSignerMethodResponseBuilder = LocalSignerMethodResponseBuilder(
                nostrEncryptionHandler = nostrEncryptionHandler,
                nostrEventSignatureHandler = eventSignatureHandler,
            ),
            internalPermissionsRepository = InternalPermissionsRepository(
                dispatchers = dispatchers,
                wellKnownApi = wellKnownApi,
            ),
            internalSessionRepository = InternalSessionRepository(
                dispatchers = dispatchers,
                database = accountDatabase,
            ),
            internalSessionEventRepository = InternalSessionEventRepository(
                dispatchers = dispatchers,
                accountDatabase = accountDatabase,
            ),
        )
    }
}
