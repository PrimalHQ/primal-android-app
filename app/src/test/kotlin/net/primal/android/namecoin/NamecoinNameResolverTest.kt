package net.primal.android.namecoin

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.android.namecoin.electrumx.NamecoinNostrResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NamecoinNameResolverTest {

    // ── isNamecoinIdentifier ───────────────────────────────────────────

    @Test
    fun `recognizes dot-bit domains`() {
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("example.bit"))
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("alice@example.bit"))
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("_@example.bit"))
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("EXAMPLE.BIT"))
    }

    @Test
    fun `recognizes d-slash names`() {
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("d/example"))
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("D/Example"))
    }

    @Test
    fun `recognizes id-slash names`() {
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("id/alice"))
        assertTrue(NamecoinNameResolver.isNamecoinIdentifier("ID/Alice"))
    }

    @Test
    fun `rejects non-namecoin identifiers`() {
        assertFalse(NamecoinNameResolver.isNamecoinIdentifier("[email protected]"))
        assertFalse(NamecoinNameResolver.isNamecoinIdentifier("npub1abc"))
        assertFalse(NamecoinNameResolver.isNamecoinIdentifier("some random text"))
        assertFalse(NamecoinNameResolver.isNamecoinIdentifier(""))
    }

    // ── Value format: simple pubkey in d/ ──────────────────────────────

    @Test
    fun `parses simple nostr field from domain value`() {
        val value = """{"nostr":"b0635d6a9851d3aed0cd6c495b282167acf761729078d975fc341b22650b07b9"}"""
        val result = extractNostrFromValue(value, "d/example", "_")
        assertNotNull(result)
        assertEquals("b0635d6a9851d3aed0cd6c495b282167acf761729078d975fc341b22650b07b9", result!!.pubkey)
    }

    // ── Value format: extended NIP-05-like in d/ ───────────────────────

    @Test
    fun `parses extended nostr names from domain value`() {
        val value = """{
            "nostr": {
                "names": {
                    "_": "aaaa000000000000000000000000000000000000000000000000000000000001",
                    "alice": "bbbb000000000000000000000000000000000000000000000000000000000002"
                },
                "relays": {
                    "bbbb000000000000000000000000000000000000000000000000000000000002": [
                        "wss://relay.example.com"
                    ]
                }
            }
        }"""

        val rootResult = extractNostrFromValue(value, "d/example", "_")
        assertNotNull(rootResult)
        assertEquals("aaaa000000000000000000000000000000000000000000000000000000000001", rootResult!!.pubkey)

        val aliceResult = extractNostrFromValue(value, "d/example", "alice")
        assertNotNull(aliceResult)
        assertEquals("bbbb000000000000000000000000000000000000000000000000000000000002", aliceResult!!.pubkey)
        assertEquals(listOf("wss://relay.example.com"), aliceResult.relays)
    }

    @Test
    fun `falls back to root when named user not found`() {
        val value = """{
            "nostr": {
                "names": {
                    "_": "aaaa000000000000000000000000000000000000000000000000000000000001"
                }
            }
        }"""

        val result = extractNostrFromValue(value, "d/example", "nonexistent")
        assertNotNull(result)
        assertEquals("aaaa000000000000000000000000000000000000000000000000000000000001", result!!.pubkey)
    }

    @Test
    fun `root lookup falls back to first entry when no underscore key`() {
        // This is the real d/testls on-chain format: only has "m", no "_"
        val value = """{
            "nostr": {
                "names": {
                    "m": "6cdebccabda1dfa058ab85352a79509b592b2bdfa0370325e28ec1cb4f18667d"
                }
            }
        }"""

        val result = extractNostrFromValue(value, "d/testls", "_")
        assertNotNull(result)
        assertEquals("6cdebccabda1dfa058ab85352a79509b592b2bdfa0370325e28ec1cb4f18667d", result!!.pubkey)
        assertEquals("m", result.localPart)
    }

    @Test
    fun `non-root lookup does NOT fall back to first entry`() {
        val value = """{
            "nostr": {
                "names": {
                    "m": "6cdebccabda1dfa058ab85352a79509b592b2bdfa0370325e28ec1cb4f18667d"
                }
            }
        }"""

        // Looking for "alice" specifically — should NOT fall back to "m"
        val result = extractNostrFromValue(value, "d/testls", "alice")
        assertNull(result)
    }

    // ── Value format: id/ namespace ────────────────────────────────────

    @Test
    fun `parses simple nostr field from identity value`() {
        val value = """{
            "nostr": "cccc000000000000000000000000000000000000000000000000000000000003",
            "email": "[email protected]"
        }"""
        val result = extractNostrFromIdentityValue(value, "id/alice")
        assertNotNull(result)
        assertEquals("cccc000000000000000000000000000000000000000000000000000000000003", result!!.pubkey)
    }

    @Test
    fun `parses object nostr field from identity value`() {
        val value = """{
            "nostr": {
                "pubkey": "dddd000000000000000000000000000000000000000000000000000000000004",
                "relays": ["wss://relay.example.com", "wss://relay2.example.com"]
            }
        }"""
        val result = extractNostrFromIdentityValue(value, "id/bob")
        assertNotNull(result)
        assertEquals("dddd000000000000000000000000000000000000000000000000000000000004", result!!.pubkey)
        assertEquals(2, result.relays.size)
    }

    // ── Invalid data ───────────────────────────────────────────────────

    @Test
    fun `rejects invalid pubkey lengths`() {
        val value = """{"nostr":"tooshort"}"""
        val result = extractNostrFromValue(value, "d/bad", "_")
        assertNull(result)
    }

    @Test
    fun `rejects non-hex pubkeys`() {
        val value = """{"nostr":"zzzz000000000000000000000000000000000000000000000000000000000000"}"""
        val result = extractNostrFromValue(value, "d/bad", "_")
        assertNull(result)
    }

    @Test
    fun `handles missing nostr field`() {
        val value = """{"ip":"1.2.3.4","map":{"www":{"ip":"1.2.3.4"}}}"""
        val result = extractNostrFromValue(value, "d/example", "_")
        assertNull(result)
    }

    @Test
    fun `handles malformed JSON gracefully`() {
        val value = "not json at all"
        val result = extractNostrFromValue(value, "d/broken", "_")
        assertNull(result)
    }

    // ── Test helpers ───────────────────────────────────────────────────

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun extractNostrFromValue(
        jsonValue: String,
        namecoinName: String,
        localPart: String,
    ): NamecoinNostrResult? {
        val obj = try {
            json.parseToJsonElement(jsonValue).jsonObject
        } catch (_: Exception) {
            return null
        }

        val nostrField = obj["nostr"] ?: return null

        if (nostrField is JsonPrimitive && nostrField.isString) {
            val pubkey = nostrField.content
            if (localPart == "_" && pubkey.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                return NamecoinNostrResult(pubkey = pubkey.lowercase(), namecoinName = namecoinName)
            }
            return null
        }

        if (nostrField is JsonObject) {
            val names = nostrField["names"]?.jsonObject ?: return null

            // Resolve: exact match → "_" root → first entry (root lookups only)
            val resolvedLocalPart: String
            val pubkey: String
            val exactMatch = names[localPart]
            val rootMatch = names["_"]
            val firstEntry = if (localPart == "_") names.entries.firstOrNull() else null

            when {
                exactMatch is JsonPrimitive && exactMatch.content.matches(Regex("^[0-9a-fA-F]{64}$")) -> {
                    resolvedLocalPart = localPart
                    pubkey = exactMatch.content
                }
                rootMatch is JsonPrimitive && rootMatch.content.matches(Regex("^[0-9a-fA-F]{64}$")) -> {
                    resolvedLocalPart = "_"
                    pubkey = rootMatch.content
                }
                firstEntry != null && firstEntry.value is JsonPrimitive &&
                    (firstEntry.value as JsonPrimitive).content.matches(Regex("^[0-9a-fA-F]{64}$")) -> {
                    resolvedLocalPart = firstEntry.key
                    pubkey = (firstEntry.value as JsonPrimitive).content
                }
                else -> return null
            }

            val relays = try {
                val relaysMap = nostrField["relays"]?.jsonObject
                relaysMap?.get(pubkey.lowercase())?.jsonArray?.mapNotNull {
                    (it as? JsonPrimitive)?.content
                } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            return NamecoinNostrResult(
                pubkey = pubkey.lowercase(),
                relays = relays,
                namecoinName = namecoinName,
                localPart = resolvedLocalPart,
            )
        }
        return null
    }

    private fun extractNostrFromIdentityValue(
        jsonValue: String,
        namecoinName: String,
    ): NamecoinNostrResult? {
        val obj = try {
            json.parseToJsonElement(jsonValue).jsonObject
        } catch (_: Exception) {
            return null
        }

        val nostrField = obj["nostr"] ?: return null

        if (nostrField is JsonPrimitive && nostrField.isString) {
            val pubkey = nostrField.content
            if (pubkey.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                return NamecoinNostrResult(pubkey = pubkey.lowercase(), namecoinName = namecoinName)
            }
        }

        if (nostrField is JsonObject) {
            val pubkey = (nostrField["pubkey"] as? JsonPrimitive)?.content
            if (pubkey != null && pubkey.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                val relays = try {
                    nostrField["relays"]?.jsonArray?.mapNotNull {
                        (it as? JsonPrimitive)?.content
                    } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                return NamecoinNostrResult(pubkey = pubkey.lowercase(), relays = relays, namecoinName = namecoinName)
            }
        }
        return null
    }
}
