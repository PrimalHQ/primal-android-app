/*
 * Application-level singleton for Namecoin name resolution.
 *
 * Ported from Amethyst PR #1734 by mstrofnone, adapted for Primal's Hilt DI.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.namecoin.electrumx.ElectrumxServer
import net.primal.android.namecoin.electrumx.NamecoinLookupCache
import net.primal.android.namecoin.electrumx.NamecoinLookupException
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.android.namecoin.electrumx.NamecoinNostrResult

/**
 * Thread-safe service for Namecoin→Nostr resolution.
 * Injected via Hilt as a singleton.
 */
@Singleton
class NamecoinNameService @Inject constructor(
    private val electrumxClient: ElectrumxClient,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Custom server list (user-configurable)
    @Volatile
    private var customServers: List<ElectrumxServer> = emptyList()

    private val resolver = NamecoinNameResolver(
        electrumxClient = electrumxClient,
        serverListProvider = { customServers.ifEmpty { ElectrumxClient.DEFAULT_SERVERS } },
    )
    private val cache = NamecoinLookupCache()

    /**
     * Resolve a Namecoin identifier to a Nostr pubkey.
     * Returns cached results when available.
     *
     * @throws NamecoinLookupException for definitive failures (not found, expired, no key, unreachable)
     */
    suspend fun resolve(identifier: String): NamecoinNostrResult {
        val cached = cache.get(identifier)
        if (cached?.result != null) return cached.result

        val result = resolver.resolve(identifier)
        cache.put(identifier, result)
        return result
    }

    /**
     * Verify that a Namecoin name maps to the expected pubkey.
     * This is the Namecoin equivalent of NIP-05 verification.
     */
    suspend fun verifyNip05(
        nip05Address: String,
        expectedPubkeyHex: String,
    ): Boolean {
        if (!NamecoinNameResolver.isNamecoinIdentifier(nip05Address)) return false
        return try {
            val result = resolve(nip05Address)
            result.pubkey.equals(expectedPubkeyHex, ignoreCase = true)
        } catch (_: NamecoinLookupException) {
            false
        }
    }

    /**
     * Update the custom server list for resolution.
     */
    fun setCustomServers(servers: List<ElectrumxServer>) {
        customServers = servers
    }

    /**
     * Clear the resolution cache.
     */
    suspend fun clearCache() = cache.clear()

    /**
     * Returns a [StateFlow] that tracks the resolution state of a Namecoin identifier.
     *
     * Useful for composable UIs that observe resolution state.
     */
    fun resolveLive(
        identifier: String,
        scope: CoroutineScope = this.scope,
    ): StateFlow<NamecoinResolveState> {
        val state = MutableStateFlow<NamecoinResolveState>(NamecoinResolveState.Loading)
        scope.launch {
            try {
                val result = resolve(identifier)
                state.value = NamecoinResolveState.Resolved(result)
            } catch (e: NamecoinLookupException.NameNotFound) {
                state.value = NamecoinResolveState.NotFound
            } catch (e: NamecoinLookupException.NameExpired) {
                state.value = NamecoinResolveState.Expired
            } catch (e: NamecoinLookupException) {
                state.value = NamecoinResolveState.Error(e.message ?: "Resolution failed")
            } catch (e: Exception) {
                state.value = NamecoinResolveState.Error(e.message ?: "Unknown error")
            }
        }
        return state
    }
}

/**
 * Observable state for a Namecoin resolution in progress.
 */
sealed class NamecoinResolveState {
    data object Loading : NamecoinResolveState()
    data class Resolved(val result: NamecoinNostrResult) : NamecoinResolveState()
    data object NotFound : NamecoinResolveState()
    data object Expired : NamecoinResolveState()
    data class Error(val message: String) : NamecoinResolveState()
}
