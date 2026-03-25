/*
 * Tests for NamecoinSettings server parsing and formatting.
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import net.primal.android.namecoin.electrumx.ElectrumxServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NamecoinSettingsTest {

    // ── Server string parsing ──────────────────────────────────────────

    @Test
    fun `parses host colon port as TLS`() {
        val s = NamecoinSettings.parseServerString("example.com:50006")
        assertNotNull(s)
        assertEquals("example.com", s!!.host)
        assertEquals(50006, s.port)
        assertTrue(s.useSsl)
    }

    @Test
    fun `parses host colon port colon tcp as plaintext`() {
        val s = NamecoinSettings.parseServerString("example.com:50001:tcp")
        assertNotNull(s)
        assertEquals("example.com", s!!.host)
        assertEquals(50001, s.port)
        assertFalse(s.useSsl)
    }

    @Test
    fun `parses onion address with trustAllCerts`() {
        val s = NamecoinSettings.parseServerString("abc123def.onion:50001:tcp")
        assertNotNull(s)
        assertEquals("abc123def.onion", s!!.host)
        assertEquals(50001, s.port)
        assertFalse(s.useSsl)
        assertTrue(s.trustAllCerts)
    }

    @Test
    fun `onion address with TLS still gets trustAllCerts`() {
        val s = NamecoinSettings.parseServerString("abc123def.onion:50002")
        assertNotNull(s)
        assertTrue(s!!.useSsl)
        assertTrue(s.trustAllCerts)
    }

    @Test
    fun `trims whitespace`() {
        val s = NamecoinSettings.parseServerString("  example.com : 50006  ")
        assertNotNull(s)
        assertEquals("example.com", s!!.host)
        assertEquals(50006, s.port)
    }

    @Test
    fun `rejects empty host`() {
        assertNull(NamecoinSettings.parseServerString(":50006"))
    }

    @Test
    fun `rejects invalid port - non-numeric`() {
        assertNull(NamecoinSettings.parseServerString("example.com:abc"))
    }

    @Test
    fun `rejects invalid port - zero`() {
        assertNull(NamecoinSettings.parseServerString("example.com:0"))
    }

    @Test
    fun `rejects invalid port - too large`() {
        assertNull(NamecoinSettings.parseServerString("example.com:99999"))
    }

    @Test
    fun `rejects no port`() {
        assertNull(NamecoinSettings.parseServerString("example.com"))
    }

    @Test
    fun `rejects blank string`() {
        assertNull(NamecoinSettings.parseServerString(""))
        assertNull(NamecoinSettings.parseServerString("   "))
    }

    // ── Format round-trip ──────────────────────────────────────────────

    @Test
    fun `formats TLS server without suffix`() {
        val server = ElectrumxServer("example.com", 50006, true)
        assertEquals("example.com:50006", NamecoinSettings.formatServerString(server))
    }

    @Test
    fun `formats TCP server with tcp suffix`() {
        val server = ElectrumxServer("example.com", 50001, false)
        assertEquals("example.com:50001:tcp", NamecoinSettings.formatServerString(server))
    }

    @Test
    fun `round-trips TLS server string through parse and format`() {
        val original = "myserver.com:50006"
        val parsed = NamecoinSettings.parseServerString(original)!!
        val formatted = NamecoinSettings.formatServerString(parsed)
        assertEquals(original, formatted)
    }

    @Test
    fun `round-trips TCP server string through parse and format`() {
        val original = "myserver.onion:50001:tcp"
        val parsed = NamecoinSettings.parseServerString(original)!!
        val formatted = NamecoinSettings.formatServerString(parsed)
        assertEquals(original, formatted)
    }

    // ── toElectrumxServers ─────────────────────────────────────────────

    @Test
    fun `returns null when no custom servers`() {
        val settings = NamecoinSettings(customServers = emptyList())
        assertNull(settings.toElectrumxServers())
    }

    @Test
    fun `returns parsed list for valid custom servers`() {
        val settings = NamecoinSettings(
            customServers = listOf(
                "server1.com:50006",
                "server2.onion:50001:tcp",
            ),
        )
        val servers = settings.toElectrumxServers()
        assertNotNull(servers)
        assertEquals(2, servers!!.size)
        assertEquals("server1.com", servers[0].host)
        assertTrue(servers[0].useSsl)
        assertEquals("server2.onion", servers[1].host)
        assertFalse(servers[1].useSsl)
        assertTrue(servers[1].trustAllCerts)
    }

    @Test
    fun `skips invalid entries in custom server list`() {
        val settings = NamecoinSettings(
            customServers = listOf(
                "valid.com:50006",
                "invalid",
                "also-invalid:abc",
            ),
        )
        val servers = settings.toElectrumxServers()
        assertNotNull(servers)
        assertEquals(1, servers!!.size)
        assertEquals("valid.com", servers[0].host)
    }

    @Test
    fun `returns null when all custom servers are invalid`() {
        val settings = NamecoinSettings(customServers = listOf("bad", "also-bad"))
        assertNull(settings.toElectrumxServers())
    }

    // ── hasCustomServers flag ──────────────────────────────────────────

    @Test
    fun `hasCustomServers is false when empty`() {
        assertFalse(NamecoinSettings().hasCustomServers)
    }

    @Test
    fun `hasCustomServers is true when populated`() {
        assertTrue(NamecoinSettings(customServers = listOf("x:1")).hasCustomServers)
    }

    // ── Default settings ───────────────────────────────────────────────

    @Test
    fun `default settings are enabled with no custom servers`() {
        val d = NamecoinSettings.DEFAULT
        assertTrue(d.enabled)
        assertTrue(d.customServers.isEmpty())
        assertFalse(d.hasCustomServers)
    }

    // ── tcp plaintext gets trustAllCerts ────────────────────────────────

    @Test
    fun `plaintext tcp server gets trustAllCerts true`() {
        val s = NamecoinSettings.parseServerString("localhost:50001:tcp")
        assertNotNull(s)
        assertFalse(s!!.useSsl)
        assertTrue(s.trustAllCerts)
    }

    @Test
    fun `TLS server without onion gets trustAllCerts false`() {
        val s = NamecoinSettings.parseServerString("example.com:50006")
        assertNotNull(s)
        assertTrue(s!!.useSsl)
        assertFalse(s.trustAllCerts)
    }
}
