package net.primal.data.account.repository.repository.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.cache.HttpCache
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.factory.AccountApiServiceFactory
import net.primal.data.account.repository.repository.BlossomRepositoryImpl
import net.primal.data.account.repository.repository.ConnectionRepositoryImpl
import net.primal.data.account.repository.repository.LocalAppRepositoryImpl
import net.primal.data.account.repository.repository.PermissionsRepositoryImpl
import net.primal.data.account.repository.repository.PushNotificationRepositoryImpl
import net.primal.data.account.repository.repository.SessionEventRepositoryImpl
import net.primal.data.account.repository.repository.SessionRepositoryImpl
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalRemoteSessionEventRepository
import net.primal.data.account.signer.remote.api.WellKnownApi
import net.primal.data.account.signer.remote.api.createWellKnownApi
import net.primal.domain.account.blossom.BlossomRepository
import net.primal.domain.account.handler.Nip46EventsHandler
import net.primal.domain.account.pushnotifications.PushNotificationRepository
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.LocalAppRepository
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

    fun createLocalAppRepository(): LocalAppRepository =
        LocalAppRepositoryImpl(
            database = resolveAccountDatabase(),
            dispatchers = dispatcherProvider,
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
            internalSessionEventRepository = InternalRemoteSessionEventRepository(
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

    fun createBlossomRepository(primalApiClient: PrimalApiClient): BlossomRepository =
        BlossomRepositoryImpl(
            dispatchers = dispatcherProvider,
            blossomsApi = AccountApiServiceFactory.createBlossomsApi(primalApiClient = primalApiClient),
        )

    fun createPushNotificationRepository(primalApiClient: PrimalApiClient): PushNotificationRepository =
        PushNotificationRepositoryImpl(
            dispatchers = dispatcherProvider,
            pushNotificationApi = AccountApiServiceFactory.createPushNotificationApi(primalApiClient = primalApiClient),
        )
}

internal expect fun provideAccountDatabase(): AccountDatabase
