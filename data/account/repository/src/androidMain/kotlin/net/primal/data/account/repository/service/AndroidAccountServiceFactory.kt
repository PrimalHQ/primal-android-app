package net.primal.data.account.repository.service

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.repository.builders.LocalSignerMethodResponseBuilder
import net.primal.data.account.repository.repository.InternalLocalSessionEventRepository
import net.primal.data.account.repository.repository.factory.provideAccountDatabase
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalSessionRepository
import net.primal.data.account.signer.remote.api.WellKnownApi
import net.primal.data.account.signer.remote.api.createWellKnownApi
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

object AndroidAccountServiceFactory {

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
            internalSessionEventRepository = InternalLocalSessionEventRepository(
                dispatchers = dispatchers,
                accountDatabase = accountDatabase,
            ),
        )
    }
}
