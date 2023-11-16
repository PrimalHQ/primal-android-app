package net.primal.android.networking.relays

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val relayPoolFactory: RelayPoolFactory,
    private val activeAccountStore: ActiveAccountStore,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val relayPoolsMutex = Mutex()

    private var regularRelaysPool: RelayPool? = null
    private var walletRelaysPool: RelayPool? = null

    init {
        observeActiveAccount()
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
        val regularRelaysChanged = regularRelaysPool?.relays != regularRelays
        if (regularRelaysChanged) {
            relayPoolsMutex.withLock {
                regularRelaysPool?.closePool()
                regularRelaysPool = null
                if (!regularRelays.isNullOrEmpty()) {
                    regularRelaysPool = relayPoolFactory.create(relays = regularRelays)
                }
            }
        }

        val walletRelaysChanged = walletRelaysPool?.relays != walletRelays
        if (walletRelaysChanged) {
            relayPoolsMutex.withLock {
                walletRelaysPool?.closePool()
                walletRelaysPool = null
                if (!walletRelays.isNullOrEmpty()) {
                    walletRelaysPool = relayPoolFactory.create(relays = walletRelays)
                }
            }
        }
    }

    private suspend fun clearRelayPools() =
        relayPoolsMutex.withLock {
            regularRelaysPool?.closePool()
            regularRelaysPool = null
            walletRelaysPool?.closePool()
            walletRelaysPool = null
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        regularRelaysPool?.publishEvent(nostrEvent) ?: throw MissingRelaysException()
    }

    suspend fun publishWalletEvent(nostrEvent: NostrEvent) {
        walletRelaysPool?.publishEvent(nostrEvent) ?: throw MissingRelaysException()
    }

    suspend fun bootstrap() {
        updateRelayPools(regularRelays = BOOTSTRAP_RELAYS, walletRelays = emptyList())
    }
}
