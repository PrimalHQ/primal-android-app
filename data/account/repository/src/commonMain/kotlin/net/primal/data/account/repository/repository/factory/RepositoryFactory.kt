package net.primal.data.account.repository.repository.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.api.WellKnownApi
import net.primal.data.account.remote.api.createWellKnownApi
import net.primal.data.account.repository.repository.ConnectionRepositoryImpl
import net.primal.data.account.repository.repository.InternalPermissionsRepository
import net.primal.data.account.repository.repository.InternalSessionEventRepository
import net.primal.data.account.repository.repository.PermissionsRepositoryImpl
import net.primal.data.account.repository.repository.SessionEventRepositoryImpl
import net.primal.data.account.repository.repository.SessionRepositoryImpl
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.domain.account.handler.Nip46EventsHandler
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository

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
            wellKnownApi = wellKnownApi,
        )

    fun createSessionRepository(): SessionRepository =
        SessionRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createSessionEventRepository(nip46EventsHandler: Nip46EventsHandler): SessionEventRepository =
        SessionEventRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
            nip46EventsHandler = nip46EventsHandler,
        )

    fun createSignerConnectionInitializer(
        connectionRepository: ConnectionRepository,
        sessionRepository: SessionRepository,
    ): SignerConnectionInitializer =
        SignerConnectionInitializer(
            connectionRepository = connectionRepository,
            sessionRepository = sessionRepository,
            internalPermissionsRepository = InternalPermissionsRepository(
                dispatchers = dispatcherProvider,
                wellKnownApi = wellKnownApi,
            ),
            internalSessionEventRepository = InternalSessionEventRepository(
                accountDatabase = resolveAccountDatabase(),
                dispatchers = dispatcherProvider,
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
