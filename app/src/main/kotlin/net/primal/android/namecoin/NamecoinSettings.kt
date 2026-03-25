/*
 * Namecoin ElectrumX server configuration.
 *
 * Ported from Amethyst PR #1786.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import net.primal.android.namecoin.electrumx.ElectrumxServer
import kotlinx.serialization.Serializable

/**
 * Immutable data class representing the current Namecoin resolution config.
 *
 * When custom servers are configured, they are used EXCLUSIVELY and the
 * hardcoded defaults are ignored. This gives privacy-conscious users full
 * control over which ElectrumX servers observe their name lookups.
 */
@Serializable
data class NamecoinSettings(
    /** Whether Namecoin resolution is enabled at all. */
    val enabled: Boolean = true,
    /**
     * Custom ElectrumX servers. When non-empty, these replace the defaults.
     *
     * Each entry is `host:port` (TLS) or `host:port:tcp` (plaintext).
     */
    val customServers: List<String> = emptyList(),
) {
    /** True when the user has configured at least one custom server. */
    val hasCustomServers: Boolean get() = customServers.isNotEmpty()

    /**
     * Convert to [ElectrumxServer] instances used by the resolver.
     * Returns `null` when no valid custom servers are configured (use defaults).
     */
    fun toElectrumxServers(): List<ElectrumxServer>? {
        if (customServers.isEmpty()) return null
        return customServers
            .mapNotNull { parseServerString(it) }
            .ifEmpty { null }
    }

    companion object {
        val DEFAULT = NamecoinSettings()

        /**
         * Parse `host:port` or `host:port:tcp` into an [ElectrumxServer].
         *
         * TLS is the default protocol. Append `:tcp` for plaintext
         * (useful for `.onion` addresses and local servers).
         *
         * `.onion` addresses automatically get `trustAllCerts = true`
         * since certificate verification is meaningless over Tor.
         */
        fun parseServerString(s: String): ElectrumxServer? {
            val parts = s.trim().split(":")
            if (parts.size < 2) return null
            val host = parts[0].trim()
            val port = parts[1].trim().toIntOrNull() ?: return null
            if (host.isEmpty() || port <= 0 || port > 65535) return null
            val useSsl = parts.getOrNull(2)?.trim()?.lowercase() != "tcp"
            val isOnion = host.endsWith(".onion")
            return ElectrumxServer(
                host = host,
                port = port,
                useSsl = useSsl,
                trustAllCerts = isOnion || !useSsl,
            )
        }

        /**
         * Format an [ElectrumxServer] back to the `host:port[:tcp]` string form.
         */
        fun formatServerString(server: ElectrumxServer): String {
            val base = "${server.host}:${server.port}"
            return if (server.useSsl) base else "$base:tcp"
        }
    }
}
