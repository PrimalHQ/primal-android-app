package net.primal.data.account.repository.repository.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.api.WellKnownApi
import net.primal.data.account.remote.api.createWellKnownApi
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.repository.ConnectionRepositoryImpl
import net.primal.data.account.repository.repository.InternalPermissionsRepository
import net.primal.data.account.repository.repository.PermissionsRepositoryImpl
import net.primal.data.account.repository.repository.SessionEventRepositoryImpl
import net.primal.data.account.repository.repository.SessionRepositoryImpl
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.nostr.cryptography.NostrKeyPair

abstract class RepositoryFactory {
    private val dispatcherProvider = createDispatcherProvider()

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

    abstract fun resolveAccountDatabase(): AccountDatabase

    fun createConnectionRepository(): ConnectionRepository =
        ConnectionRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createSessionRepository(): SessionRepository =
        SessionRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createSessionEventRepository(): SessionEventRepository =
        SessionEventRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createSignerConnectionInitializer(
        signerKeyPair: NostrKeyPair,
        connectionRepository: ConnectionRepository,
    ): SignerConnectionInitializer =
        SignerConnectionInitializer(
            connectionRepository = connectionRepository,
            internalPermissionsRepository = InternalPermissionsRepository(
                dispatchers = dispatcherProvider,
                wellKnownApi = wellKnownApi,
            ),
            nostrRelayManager = NostrRelayManager(
                dispatcherProvider = dispatcherProvider,
                signerKeyPair = signerKeyPair,
            ),
        )

    fun createPermissionsRepository(): PermissionsRepository =
        PermissionsRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
            wellKnownApi = wellKnownApi,
        )
}

internal expect fun provideAccountDatabase(): AccountDatabase
