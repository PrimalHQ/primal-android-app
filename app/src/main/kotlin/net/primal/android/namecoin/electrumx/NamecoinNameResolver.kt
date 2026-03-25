/*
 * Namecoin name resolver for NIP-05 identity verification.
 *
 * Ported from Amethyst PR #1734 by mstrofnone.
 * Original: https://github.com/vitorpamplona/amethyst/pull/1734
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.electrumx

import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Result of resolving a Namecoin name to Nostr identity data.
 */
data class NamecoinNostrResult(
    val pubkey: String,
    val relays: List<String> = emptyList(),
    val namecoinName: String,
    val localPart: String = "_",
)

/**
 * Resolves Namecoin names (.bit, d/, id/) to Nostr public keys
 * via ElectrumX blockchain lookups.
 */
class NamecoinNameResolver(
    private val electrumxClient: ElectrumxClient = ElectrumxClient(),
    private val lookupTimeoutMs: Long = 20_000L,
    private val serverListProvider: () -> List<ElectrumxServer> = { ElectrumxClient.DEFAULT_SERVERS },
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private val HEX_PUBKEY_REGEX = Regex("^[0-9a-fA-F]{64}$")

        fun isNamecoinIdentifier(identifier: String): Boolean {
            val normalized = identifier.trim().lowercase()
            return normalized.endsWith(".bit") ||
                normalized.startsWith("d/") ||
                normalized.startsWith("id/")
        }
    }

    /**
     * Resolve a Namecoin identifier to a Nostr pubkey.
     *
     * @throws NamecoinLookupException for all definitive failure types
     * @throws kotlinx.coroutines.TimeoutCancellationException if resolution takes too long
     */
    suspend fun resolve(identifier: String): NamecoinNostrResult {
        val parsed = parseIdentifier(identifier)
            ?: throw NamecoinLookupException.NameNotFound(identifier)
        return withTimeout(lookupTimeoutMs) {
            performLookup(parsed)
        }
    }

    // ── Identifier Parsing ─────────────────────────────────────────────

    private data class ParsedIdentifier(
        val namecoinName: String,
        val localPart: String,
        val namespace: Namespace,
    )

    private enum class Namespace { DOMAIN, IDENTITY }

    private fun parseIdentifier(raw: String): ParsedIdentifier? {
        val input = raw.trim()

        if (input.startsWith("d/", ignoreCase = true)) {
            return ParsedIdentifier(input.lowercase(), "_", Namespace.DOMAIN)
        }
        if (input.startsWith("id/", ignoreCase = true)) {
            return ParsedIdentifier(input.lowercase(), "_", Namespace.IDENTITY)
        }

        if (input.contains("@") && input.endsWith(".bit", ignoreCase = true)) {
            val parts = input.split("@", limit = 2)
            if (parts.size != 2) return null
            val localPart = parts[0].lowercase().ifEmpty { "_" }
            val domain = parts[1].removeSuffix(".bit").lowercase()
            if (domain.isEmpty()) return null
            return ParsedIdentifier("d/$domain", localPart, Namespace.DOMAIN)
        }

        if (input.endsWith(".bit", ignoreCase = true)) {
            val domain = input.removeSuffix(".bit").lowercase()
            if (domain.isEmpty()) return null
            return ParsedIdentifier("d/$domain", "_", Namespace.DOMAIN)
        }

        return null
    }

    // ── Lookup & Value Parsing ─────────────────────────────────────────

    /**
     * @throws NamecoinLookupException for definitive failures (not found, expired, unreachable)
     * @throws NamecoinLookupException.NoNostrKey if the name exists but has no valid Nostr pubkey
     */
    private suspend fun performLookup(parsed: ParsedIdentifier): NamecoinNostrResult {
        // nameShowWithFallback throws NamecoinLookupException on failure
        val nameResult = electrumxClient.nameShowWithFallback(parsed.namecoinName, serverListProvider())
        val valueJson = tryParseJson(nameResult.value)
            ?: throw NamecoinLookupException.NoNostrKey(parsed.namecoinName)

        return when (parsed.namespace) {
            Namespace.DOMAIN -> extractFromDomainValue(valueJson, parsed)
            Namespace.IDENTITY -> extractFromIdentityValue(valueJson, parsed)
        } ?: throw NamecoinLookupException.NoNostrKey(parsed.namecoinName)
    }

    private fun extractFromDomainValue(value: JsonObject, parsed: ParsedIdentifier): NamecoinNostrResult? {
        val nostrField = value["nostr"] ?: return null

        // Simple form: "nostr": "hex-pubkey" (only valid for root lookups)
        if (nostrField is JsonPrimitive && nostrField.isString) {
            val pubkey = nostrField.content
            if (parsed.localPart == "_" && isValidPubkey(pubkey)) {
                return NamecoinNostrResult(pubkey.lowercase(), namecoinName = parsed.namecoinName, localPart = "_")
            }
            if (parsed.localPart != "_") return null
        }

        // Extended form: "nostr": { "names": {...}, "relays": {...} }
        if (nostrField is JsonObject) {
            val names = nostrField["names"]?.jsonObject ?: return null

            // Resolve the local part and pubkey:
            // 1. Try exact match for the requested local part
            // 2. Fall back to "_" root entry
            // 3. For root lookups (localPart == "_"), fall back to first available entry
            val resolvedLocalPart: String
            val pubkey: String

            val exactMatch = names[parsed.localPart]
            val rootMatch = names["_"]
            val firstEntry = if (parsed.localPart == "_") names.entries.firstOrNull() else null

            when {
                exactMatch is JsonPrimitive && isValidPubkey(exactMatch.content) -> {
                    resolvedLocalPart = parsed.localPart
                    pubkey = exactMatch.content
                }
                rootMatch is JsonPrimitive && isValidPubkey(rootMatch.content) -> {
                    resolvedLocalPart = "_"
                    pubkey = rootMatch.content
                }
                firstEntry != null && firstEntry.value is JsonPrimitive &&
                    isValidPubkey((firstEntry.value as JsonPrimitive).content) -> {
                    resolvedLocalPart = firstEntry.key
                    pubkey = (firstEntry.value as JsonPrimitive).content
                }
                else -> return null
            }

            val relays = extractRelays(nostrField, pubkey)
            return NamecoinNostrResult(
                pubkey = pubkey.lowercase(),
                relays = relays,
                namecoinName = parsed.namecoinName,
                localPart = resolvedLocalPart,
            )
        }
        return null
    }

    private fun extractFromIdentityValue(value: JsonObject, parsed: ParsedIdentifier): NamecoinNostrResult? {
        val nostrField = value["nostr"] ?: return null

        if (nostrField is JsonPrimitive && nostrField.isString) {
            val pubkey = nostrField.content
            if (isValidPubkey(pubkey)) {
                return NamecoinNostrResult(pubkey.lowercase(), namecoinName = parsed.namecoinName)
            }
        }

        if (nostrField is JsonObject) {
            val pubkey = (nostrField["pubkey"] as? JsonPrimitive)?.content
            if (pubkey != null && isValidPubkey(pubkey)) {
                val relays = try {
                    nostrField["relays"]?.jsonArray?.mapNotNull { (it as? JsonPrimitive)?.content } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                return NamecoinNostrResult(pubkey.lowercase(), relays, parsed.namecoinName)
            }

            val names = nostrField["names"]?.jsonObject
            if (names != null) {
                val rootPubkey = (names["_"] as? JsonPrimitive)?.content
                if (rootPubkey != null && isValidPubkey(rootPubkey)) {
                    val relays = extractRelays(nostrField, rootPubkey)
                    return NamecoinNostrResult(rootPubkey.lowercase(), relays, parsed.namecoinName)
                }
            }
        }
        return null
    }

    private fun extractRelays(nostrObj: JsonObject, pubkey: String): List<String> {
        return try {
            val relaysMap = nostrObj["relays"]?.jsonObject ?: return emptyList()
            val relayArray = relaysMap[pubkey.lowercase()]?.jsonArray
                ?: relaysMap[pubkey]?.jsonArray
                ?: return emptyList()
            relayArray.mapNotNull { (it as? JsonPrimitive)?.content }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun tryParseJson(raw: String): JsonObject? = try {
        json.parseToJsonElement(raw).jsonObject
    } catch (_: Exception) {
        null
    }

    private fun isValidPubkey(s: String): Boolean = HEX_PUBKEY_REGEX.matches(s)
}
