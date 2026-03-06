/*
 * Application-level singleton for Namecoin name resolution.
 *
 * Ported from Amethyst PR #1734 by mstrofnone, adapted for Primal's Hilt DI.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.namecoin.electrumx.ElectrumxServer
import net.primal.android.namecoin.electrumx.NamecoinLookupCache
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.android.namecoin.electrumx.NamecoinNostrResult

/**
 * Thread-safe service for Namecoin→Nostr resolution.
 * Injected via Hilt as a singleton.
 */
@Singleton
class NamecoinNameService @Inject constructor(
    private val resolver: NamecoinNameResolver,
) {
    private val cache = NamecoinLookupCache()

    /**
     * Resolve a Namecoin identifier to a Nostr pubkey.
     * Returns cached results when available.
     */
    suspend fun resolve(identifier: String): NamecoinNostrResult? {
        val cached = cache.get(identifier)
        if (cached != null) return cached.result

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
        val result = resolve(nip05Address) ?: return false
        return result.pubkey.equals(expectedPubkeyHex, ignoreCase = true)
    }

    /**
     * Clear the resolution cache.
     */
    suspend fun clearCache() = cache.clear()
}

/**
 * Observable state for a Namecoin resolution in progress.
 */
sealed class NamecoinResolveState {
    data object Loading : NamecoinResolveState()
    data class Resolved(val result: NamecoinNostrResult) : NamecoinResolveState()
    data object NotFound : NamecoinResolveState()
    data class Error(val message: String) : NamecoinResolveState()
}
