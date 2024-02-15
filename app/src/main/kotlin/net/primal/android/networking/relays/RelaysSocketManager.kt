package net.primal.android.networking.relays

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.mapToRelayDO

@Singleton
class RelaysSocketManager @Inject constructor(
    dispatchers: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val primalDatabase: PrimalDatabase,
    private val userRelaysPool: RelayPool,
    private val nwcRelaysPool: RelayPool,
    private val bootstrapRelays: RelayPool,
) {
    private val scope = CoroutineScope(dispatchers.io())
    private val relayPoolsMutex = Mutex()

    private var relaysObserverJob: Job? = null

    val userRelayPoolStatus = userRelaysPool.relayPoolStatus

    init {
        initBootstrapRelaysPool()
        observeActiveAccount()
    }

    private fun initBootstrapRelaysPool() {
        bootstrapRelays.changeRelays(BOOTSTRAP_RELAYS)
    }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeAccountState.collect { activeAccountState ->
                when (activeAccountState) {
                    is ActiveUserAccountState.ActiveUserAccount -> {
                        val data = activeAccountState.data
                        relaysObserverJob?.cancel()
                        relaysObserverJob = observeRelays(data.pubkey)
                    }

                    ActiveUserAccountState.NoUserAccount -> {
                        relaysObserverJob?.cancel()
                        relaysObserverJob = null
                        clearRelayPools()
                    }
                }
            }
        }

    private fun observeRelays(userId: String): Job =
        scope.launch {
            primalDatabase.relays().observeRelays(userId = userId).collect { relays ->
                val userRelays = relays.filter { it.kind == RelayKind.UserRelay }.map { it.mapToRelayDO() }
                val nwcRelays = relays.filter { it.kind == RelayKind.NwcRelay }.map { it.mapToRelayDO() }
                updateRelayPools(regularRelays = userRelays, walletRelays = nwcRelays)
            }
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

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        if (userRelaysPool.hasRelays()) {
            userRelaysPool.publishEvent(nostrEvent)
        } else {
            bootstrapRelays.publishEvent(nostrEvent)
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishNwcEvent(nostrEvent: NostrEvent) {
        if (!nwcRelaysPool.hasRelays()) {
            throw NostrPublishException(cause = IllegalStateException("nwc relay not found"))
        }

        nwcRelaysPool.publishEvent(nostrEvent)
    }

    suspend fun ensureUserRelayPoolConnected() = userRelaysPool.ensureAllRelaysConnected()

    suspend fun ensureUserRelayConnected(url: String) = userRelaysPool.ensureRelayConnected(url)
}
