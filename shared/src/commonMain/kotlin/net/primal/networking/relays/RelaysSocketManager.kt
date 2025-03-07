package net.primal.networking.relays

import io.ktor.client.HttpClient
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.coroutines.DispatcherProvider
import net.primal.networking.model.NostrEvent
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.relays.errors.NostrPublishException

internal class RelaysSocketManager(
    private val dispatchers: DispatcherProvider,
    private val httpClient: HttpClient,
    private val primalApiClient: PrimalApiClient, //TODO @PrimalCacheApiClient
//    private val activeAccountStore: ActiveAccountStore,
//    private val primalDatabase: PrimalDatabase,
) {
    private val scope = CoroutineScope(dispatchers.io())
    private val relayPoolsMutex = Mutex()

    private var relaysObserverJob: Job? = null

    private fun buildRelayPool() =
        RelayPool(
            dispatchers = dispatchers,
            httpClient = httpClient,
            primalApiClient = primalApiClient,
        )

    private val userRelaysPool: RelayPool = buildRelayPool()
    private val nwcRelaysPool: RelayPool = buildRelayPool()
    private val fallbackRelaysPool: RelayPool = buildRelayPool()

    val userRelayPoolStatus = userRelaysPool.relayPoolStatus

    init {
        // TODO Double check launching coroutine for init fallback relays
        scope.launch { initFallbackRelaysPool() }
        observeActiveUserId()
    }

    private suspend fun initFallbackRelaysPool() = fallbackRelaysPool.changeRelays(FALLBACK_RELAYS)

    private fun observeActiveUserId() =
        scope.launch {
            // TODO Fix coupling with active account
//            activeAccountStore.activeUserId.collect { userId ->
            val userId = ""
                when {
                    userId.isEmpty() -> {
                        relaysObserverJob?.cancel()
                        relaysObserverJob = null
                        clearRelayPools()
                    }

                    else -> {
                        relaysObserverJob?.cancel()
                        relaysObserverJob = observeRelays(userId)
                    }
                }
//            }
        }

    // TODO Fix this
//    private suspend fun isCachingProxyEnabled() = activeAccountStore.activeUserAccount().cachingProxyEnabled
    private fun isCachingProxyEnabled() = false

    private fun observeRelays(userId: String): Job =
        scope.launch {
            // Fix observeRelays
//            try {
//                primalDatabase.relays().observeRelays(userId = userId).collect { relays ->
//                    val userRelays = relays.filter { it.kind == RelayKind.UserRelay }.map { it.mapToRelayDO() }
//                    val nwcRelays = relays.filter { it.kind == RelayKind.NwcRelay }.map { it.mapToRelayDO() }
//                    updateRelayPools(regularRelays = userRelays, walletRelays = nwcRelays)
//                }
//            } catch (error: CancellationException) {
//                Napier.w(error) { "failed observing user relays" }
//            }
        }

    private suspend fun updateRelayPools(regularRelays: List<Relay>?, walletRelays: List<Relay>?) {
        relayPoolsMutex.withLock {
            val userRelaysChanged = userRelaysPool.relays != regularRelays
            if (userRelaysChanged && !regularRelays.isNullOrEmpty()) {
                userRelaysPool.changeRelays(relays = regularRelays)
            }

            val nwcRelaysChanged = nwcRelaysPool.relays != walletRelays
            if (nwcRelaysChanged && !walletRelays.isNullOrEmpty()) {
                nwcRelaysPool.changeRelays(relays = walletRelays)
            }
        }
    }

    private suspend fun clearRelayPools() =
        relayPoolsMutex.withLock {
            userRelaysPool.closePool()
            nwcRelaysPool.closePool()
        }

    @Throws(NostrPublishException::class, CancellationException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        if (userRelaysPool.hasRelays()) {
            userRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        } else {
            fallbackRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        }
    }

    @Throws(NostrPublishException::class, CancellationException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent, relays: List<Relay>) {
        val customPool = buildRelayPool()
        customPool.changeRelays(relays = relays)
        customPool.ensureAllRelaysConnected()
        customPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        customPool.closePool()
    }

    @Throws(NostrPublishException::class, CancellationException::class)
    suspend fun publishNwcEvent(nostrEvent: NostrEvent) {
        if (!nwcRelaysPool.hasRelays()) {
            throw NostrPublishException(cause = IllegalStateException("nwc relay not found"))
        }

        nwcRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
    }

    suspend fun ensureUserRelayPoolConnected() = userRelaysPool.ensureAllRelaysConnected()

    suspend fun ensureUserRelayConnected(url: String) = userRelaysPool.ensureRelayConnected(url)
}
