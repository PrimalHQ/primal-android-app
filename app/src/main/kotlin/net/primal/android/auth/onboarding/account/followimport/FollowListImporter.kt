/*
 * Follow list importer — resolves identifiers and fetches kind 3 events.
 *
 * Ported from Amethyst PR #1785 by mstrofnone, adapted for Primal's architecture.
 * Original: https://github.com/vitorpamplona/amethyst/pull/1785
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.auth.onboarding.account.followimport

import net.primal.android.namecoin.electrumx.NamecoinLookupException
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.domain.nostr.cryptography.utils.Bech32
import net.primal.domain.nostr.cryptography.utils.toHex

/**
 * A single entry from a follow list.
 */
data class FollowEntry(
    val pubkeyHex: String,
    val relayHint: String? = null,
    val petname: String? = null,
)

/**
 * Result of fetching a user's follow list.
 */
sealed class FollowListResult {
    data class Success(
        val sourcePubkeyHex: String,
        val follows: List<FollowEntry>,
        val createdAt: Long,
        /** If the identifier was resolved via Namecoin, this holds the .bit name */
        val resolvedViaNamecoin: String? = null,
    ) : FollowListResult()

    data object NoFollowList : FollowListResult()

    data class InvalidIdentifier(val reason: String) : FollowListResult()

    data class Error(val message: String) : FollowListResult()
}

/**
 * Internal: result of resolving an identifier, tracking whether Namecoin was used.
 */
data class ResolvedIdentifier(
    val pubkeyHex: String,
    val namecoinSource: String? = null,
)

/**
 * Fetches another user's follow list.
 *
 * Resolution order for identifiers:
 *   1. Namecoin (.bit / d/ / id/) → ElectrumX blockchain query
 *   2. Hex pubkey (64 hex chars)
 *   3. npub1... (NIP-19 bech32)
 *   4. NIP-05 (user@domain) → HTTP /.well-known/nostr.json
 *
 * @param resolveNamecoin Optional Namecoin resolver returning hex pubkey or null.
 */
class FollowListImporter(
    private val resolveNamecoin: (suspend (String) -> String?)? = null,
) {
    companion object {
        private val HEX_PUBKEY_REGEX = Regex("^[0-9a-fA-F]{64}$")
        private const val NPUB_PREFIX = "npub1"
    }

    /**
     * Resolve an identifier to a hex pubkey.
     */
    suspend fun resolveIdentifier(
        identifier: String,
        resolveNip05: (suspend (String) -> String?)? = null,
    ): ResolvedIdentifier? {
        val trimmed = identifier.trim()

        // 1. Namecoin
        if (resolveNamecoin != null && NamecoinNameResolver.isNamecoinIdentifier(trimmed)) {
            val pubkey = resolveNamecoin.invoke(trimmed)
            return if (pubkey != null) ResolvedIdentifier(pubkey, namecoinSource = trimmed) else null
        }

        // 2. Direct hex pubkey
        if (HEX_PUBKEY_REGEX.matches(trimmed)) {
            return ResolvedIdentifier(trimmed.lowercase())
        }

        // 3. NIP-19 npub
        if (trimmed.startsWith(NPUB_PREFIX, ignoreCase = true)) {
            return try {
                val (_, data) = Bech32.decodeBytes(trimmed)
                if (data.size == 32) ResolvedIdentifier(data.toHex()) else null
            } catch (_: Exception) {
                null
            }
        }

        // 4. NIP-05 (user@domain)
        if (trimmed.contains("@") && resolveNip05 != null) {
            val pk = resolveNip05(trimmed)
            if (pk != null) return ResolvedIdentifier(pk)
        }

        // 5. Bare string — try as NIP-05
        if (resolveNip05 != null && !trimmed.startsWith("nsec")) {
            val pk = resolveNip05(trimmed)
            if (pk != null) return ResolvedIdentifier(pk)
        }

        return null
    }

    /**
     * Build a detailed error message for a failed Namecoin resolution.
     */
    fun namecoinErrorMessage(identifier: String, exception: NamecoinLookupException): String {
        return when (exception) {
            is NamecoinLookupException.NameNotFound ->
                "Namecoin name \"$identifier\" does not exist on the blockchain. " +
                    "Check the spelling or register it with Electrum-NMC."

            is NamecoinLookupException.NameExpired ->
                "Namecoin name \"$identifier\" has expired on the blockchain."

            is NamecoinLookupException.NoNostrKey ->
                "Namecoin name \"$identifier\" exists but has no \"nostr\" field. " +
                    "The owner needs to add a nostr pubkey to the name's value."

            is NamecoinLookupException.ServersUnreachable ->
                "All Namecoin ElectrumX servers are unreachable. " +
                    "Check your internet connection and try again."

            is NamecoinLookupException.ParseError ->
                "Failed to parse Namecoin response for \"$identifier\"."
        }
    }
}
