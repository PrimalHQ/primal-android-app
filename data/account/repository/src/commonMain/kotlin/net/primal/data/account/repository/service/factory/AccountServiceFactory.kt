package net.primal.data.account.repository.service.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.repository.builder.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.manager.RemoteAppConnectionManager
import net.primal.data.account.repository.repository.factory.provideAccountDatabase
import net.primal.data.account.repository.repository.internal.InternalRemoteSessionEventRepository
import net.primal.data.account.repository.service.RemoteSignerServiceImpl
import net.primal.data.account.signer.remote.api.WellKnownApi
import net.primal.data.account.signer.remote.api.createWellKnownApi
import net.primal.data.account.signer.remote.signer.parser.RemoteSignerMethodParser
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair

object AccountServiceFactory {

    private val remoteAppConnectionManager = RemoteAppConnectionManager()

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
            internalSessionEventRepository = InternalRemoteSessionEventRepository(
                dispatchers = dispatchers,
                accountDatabase = accountDatabase,
            ),
            remoteSignerMethodParser = RemoteSignerMethodParser(
                nostrEncryptionService = nostrEncryptionService,
            ),
            remoteAppConnectionManager = remoteAppConnectionManager,
        )
    }
}
