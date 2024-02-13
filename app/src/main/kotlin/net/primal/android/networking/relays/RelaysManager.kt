package net.primal.android.networking.relays

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.accounts.BOOTSTRAP_RELAYS
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toRelay

@Singleton
class RelaysManager @Inject constructor(
    dispatchers: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val regularRelaysPool: RelayPool,
    private val walletRelaysPool: RelayPool,
    private val bootstrapRelays: RelayPool,
) {
    private val scope = CoroutineScope(dispatchers.io())
    private val relayPoolsMutex = Mutex()

    val regularRelayPoolStatus = regularRelaysPool.relayPoolStatus

    init {
        observeActiveAccount()
        initBootstrapRelaysPool()
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
                        updateRelayPools(
                            regularRelays = data.relays,
                            walletRelays = data.nostrWallet?.relays?.map { it.toRelay() },
                        )
                    }

                    ActiveUserAccountState.NoUserAccount -> {
                        clearRelayPools()
                    }
                }
            }
        }

    private suspend fun updateRelayPools(regularRelays: List<Relay>?, walletRelays: List<Relay>?) {
        relayPoolsMutex.withLock {
            val regularRelaysChanged = regularRelaysPool.relays != regularRelays
            if (regularRelaysChanged && !regularRelays.isNullOrEmpty()) {
                regularRelaysPool.changeRelays(relays = regularRelays)
            }

            val walletRelaysChanged = walletRelaysPool.relays != walletRelays
            if (walletRelaysChanged && !walletRelays.isNullOrEmpty()) {
                walletRelaysPool.changeRelays(relays = walletRelays)
            }
        }
    }

    private suspend fun clearRelayPools() =
        relayPoolsMutex.withLock {
            regularRelaysPool.closePool()
            walletRelaysPool.closePool()
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        if (regularRelaysPool.hasRelays()) {
            regularRelaysPool.publishEvent(nostrEvent)
        } else {
            bootstrapRelays.publishEvent(nostrEvent)
        }
    }

    @Throws(NostrPublishException::class, MissingRelaysException::class)
    suspend fun publishWalletEvent(nostrEvent: NostrEvent) {
        if (!walletRelaysPool.hasRelays()) throw MissingRelaysException()

        walletRelaysPool.publishEvent(nostrEvent)
    }

    suspend fun bootstrap() {
        updateRelayPools(regularRelays = BOOTSTRAP_RELAYS, walletRelays = emptyList())
    }

    suspend fun ensureUserRelayPoolConnected() {
        regularRelaysPool.ensureConnected()
    }
}
