/*
 * Namecoin ElectrumX client for NIP-05 identity verification.
 *
 * Ported from Amethyst PR #1734 by mstrofnone.
 * Cert pinning + TLS hardening from Amethyst PR #1937.
 * Original: https://github.com/vitorpamplona/amethyst/pull/1734
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.electrumx

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Result of an ElectrumX name_show query.
 */
@Serializable
data class NameShowResult(
    val name: String,
    val value: String,
    val txid: String? = null,
    val height: Int? = null,
    val expiresIn: Int? = null,
)

/**
 * Represents a single ElectrumX server endpoint.
 */
data class ElectrumxServer(
    val host: String,
    val port: Int,
    val useSsl: Boolean = true,
    val trustAllCerts: Boolean = false,
)

/**
 * Result of testing connectivity to a single ElectrumX server.
 */
data class ServerTestResult(
    val server: ElectrumxServer,
    val success: Boolean,
    val responseTimeMs: Long,
    val error: String? = null,
    val tlsVersion: String? = null,
    /** PEM-encoded server certificate, captured during test for TOFU pinning. */
    val serverCertPem: String? = null,
    /** SHA-256 fingerprint of the server certificate. */
    val certFingerprint: String? = null,
)

/**
 * Lightweight ElectrumX client for Namecoin name resolution.
 *
 * Connects over TCP/TLS to a Namecoin ElectrumX server and resolves
 * Namecoin names to their current values using scripthash-based lookups.
 *
 * Uses pinned certificates for known ElectrumX servers instead of a
 * trust-all TrustManager. This is required because Samsung One UI 7
 * (Android 16) silently rejects connections that use no-op trust managers.
 */
class ElectrumxClient(
    private val connectTimeoutMs: Long = 10_000L,
    private val readTimeoutMs: Long = 15_000L,
    private val socketFactory: () -> SocketFactory = { SocketFactory.getDefault() },
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val requestId = AtomicInteger(0)
    /** Per-server mutexes to avoid one slow server blocking queries to others. */
    private val serverMutexes = ConcurrentHashMap<String, Mutex>()

    /** User-supplied PEM certificates for custom servers (TOFU-pinned). */
    private val dynamicCerts = mutableListOf<String>()

    /** Lazy-cached SSLSocketFactory for pinned certs. Thread-safe via volatile + DCL. */
    @Volatile
    private var pinnedFactory: SSLSocketFactory? = null

    companion object {
        val DEFAULT_SERVERS = listOf(
            ElectrumxServer("electrumx.testls.space", 50002, useSsl = true, trustAllCerts = true),
            ElectrumxServer("nmc2.bitcoins.sk", 57002, useSsl = true, trustAllCerts = true),
            ElectrumxServer("46.229.238.187", 57002, useSsl = true, trustAllCerts = true),
        )

        val TOR_SERVERS = listOf(
            ElectrumxServer(
                "i665jpwsq46zlsdbnj4axgzd3s56uzey5uhotsnxzsknzbn36jaddsid.onion",
                50002, useSsl = true, trustAllCerts = true,
            ),
            ElectrumxServer("electrumx.testls.space", 50002, useSsl = true, trustAllCerts = true),
            ElectrumxServer("nmc2.bitcoins.sk", 57002, useSsl = true, trustAllCerts = true),
        )

        private const val PROTOCOL_VERSION = "1.4"
        const val NAME_EXPIRE_DEPTH = 36_000

        // Namecoin script opcodes
        private const val OP_NAME_UPDATE: Byte = 0x53
        private const val OP_2DROP: Byte = 0x6d
        private const val OP_DROP: Byte = 0x75
        private const val OP_RETURN: Byte = 0x6a
        private const val OP_PUSHDATA1: Byte = 0x4c
        private const val OP_PUSHDATA2: Byte = 0x4d

        /**
         * PEM-encoded certificates for the well-known Namecoin ElectrumX servers.
         *
         * These are self-signed certificates that cannot be verified by the
         * system CA store. We pin them explicitly so that connections succeed
         * on devices with strict TLS enforcement (e.g. Samsung One UI 7).
         *
         * To update: `echo | openssl s_client -connect HOST:PORT 2>/dev/null | openssl x509 -outform PEM`
         */
        private val PINNED_ELECTRUMX_CERTS = listOf(
            // electrumx.testls.space:50002 — expires 2027-05-04
            // Also covers the .onion hidden service (same operator, same cert):
            // i665jpwsq46zlsdbnj4axgzd3s56uzey5uhotsnxzsknzbn36jaddsid.onion:50002
            // SHA-256: 53:65:D5:BB:26:19:F5:40:1C:D8:8E:FC:AF:FB:A5:B2:A0:EA:7A:99:2D:F7:0F:05:7E:9B:CD:50:36:C7:79:9C
            """
-----BEGIN CERTIFICATE-----
MIIDwzCCAqsCFGGKT5mjh7oN98aNyjOCiqafL8VyMA0GCSqGSIb3DQEBCwUAMIGd
MQswCQYDVQQGEwJVUzEQMA4GA1UECAwHQ2hpY2FnbzEQMA4GA1UEBwwHQ2hpY2Fn
bzESMBAGA1UECgwJSW50ZXJuZXRzMQ8wDQYDVQQLDAZJbnRlcncxHjAcBgNVBAMM
FWVsZWN0cnVtLnRlc3Rscy5zcGFjZTElMCMGCSqGSIb3DQEJARYWbWpfZ2lsbF84
OUBob3RtYWlsLmNvbTAeFw0yMjA1MDUwNjIzNDFaFw0yNzA1MDQwNjIzNDFaMIGd
MQswCQYDVQQGEwJVUzEQMA4GA1UECAwHQ2hpY2FnbzEQMA4GA1UEBwwHQ2hpY2Fn
bzESMBAGA1UECgwJSW50ZXJuZXRzMQ8wDQYDVQQLDAZJbnRlcncxHjAcBgNVBAMM
FWVsZWN0cnVtLnRlc3Rscy5zcGFjZTElMCMGCSqGSIb3DQEJARYWbWpfZ2lsbF84
OUBob3RtYWlsLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAO4H
+PKCdiiz3jNOA77aAmS2YaU7eOQ8ZGliEVr/PlLcgF5gmthb2DI6iK4KhC1ad34G
1n9IhkXPhkVJ94i8wB3uoTBlA7mI5h59m01yhzSkJAoYoU/i6DM9ipbakqWFCTEp
P+yE216NTU5MbYwThZdRSAIIABe9RyIliMSidyrwHvKBLfnJPFScghW6rhBWN7PG
PA8k0MFGzf+HXbpnV/jAvz08ZC34qiBIjkJrTgh49JweyoZKdppyJcH4UbkslJ2t
YUJR3oURBvrPj+D7TwLVRbX36ul7r4+dP3IjgmljsSAHDK4N/PfWrCBdlj9Pc1Cp
yX+ZDh8X2NrL4ukHoVMCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAeVj6VZNmY/Vb
nhzrC7xBSHqVWQ1wkLOClLsdvgKP8cFFJuUoCMQU5bPMi7nWnkfvvsIKH4Eibk5K
fqiA9jVsY0FHvQ8gP3KMk1LVuUf/sTcRe5itp3guBOSk/zXZUD5tUz/oRk3k+rdc
MsInqhomjNy/dqYmD6Wm4DNPjZh6fWy+AVQKVNOI2t4koaVdpoi8Uv8h4gFGPbdI
sVmtoGiIGkKNIWum+6mnF6PfynNrLk+ztH4TrdacVNeoJUPYEAxOuesWXFy3H4r+
HKBqA4xAzyjgKLPqoWnjSu7gxj1GIjBhnDxkM6wUOnDq8A0EqxR+A17OcXW9sZ2O
2ZIVwmtnyA==
-----END CERTIFICATE-----
            """.trimIndent(),
            // nmc2.bitcoins.sk:57002 / 46.229.238.187:57002 — expires 2030-10-22
            """
-----BEGIN CERTIFICATE-----
MIID+TCCAuGgAwIBAgIUdmJGukmfPvqmAYpTfuGcjRoYHJ8wDQYJKoZIhvcNAQEL
BQAwgYsxCzAJBgNVBAYTAlNLMREwDwYDVQQIDAhTbG92YWtpYTETMBEGA1UEBwwK
QnJhdGlzbGF2YTEUMBIGA1UECgwLYml0Y29pbnMuc2sxGTAXBgNVBAMMEG5tYzIu
Yml0Y29pbnMuc2sxIzAhBgkqhkiG9w0BCQEWFGRlYWZib3lAY2ljb2xpbmEub3Jn
MB4XDTIwMTAyNDE5MjQzOVoXDTMwMTAyMjE5MjQzOVowgYsxCzAJBgNVBAYTAlNL
MREwDwYDVQQIDAhTbG92YWtpYTETMBEGA1UEBwwKQnJhdGlzbGF2YTEUMBIGA1UE
CgwLYml0Y29pbnMuc2sxGTAXBgNVBAMMEG5tYzIuYml0Y29pbnMuc2sxIzAhBgkq
hkiG9w0BCQEWFGRlYWZib3lAY2ljb2xpbmEub3JnMIIBIjANBgkqhkiG9w0BAQEF
AAOCAQ8AMIIBCgKCAQEAzBUkZNDfaz7kc28l5tDKohJjekWmz1ynzfGx3ZLsqOZE
c+kNfcMaWU+zT/j0mV6pX6KSH7G9pPAku+8PRdKRq+d63wiJDEjGSaFztQWKW6L1
vTxgCK5gu+Eir3BkTagJObsrLKS+T6qH610/3+btGgoR3lunB5TzCgB/9oQanjDW
zjg2CwmxgR5Iw1Eqfenx7zkSK33FSXSF2SvbUs1Atj2oPU4DLivyrx0RaUmaPemn
cmcpnax+py4pQeB6dJWU1INhzXt3hTJRyoqsSGY3vCECIKIBIkh8GsYjAX4z+Y9y
6pJx0da2b88qPWdsoxaIMvrQiuWknDrSJwAyw2Yd8QIDAQABo1MwUTAdBgNVHQ4E
FgQUT2J83B2/9jxGGdFeWrxMohTzHNwwHwYDVR0jBBgwFoAUT2J83B2/9jxGGdFe
WrxMohTzHNwwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAsbxX
wN8tZaXOybImMZCQS7zfxmKl2IAcqu+R01KPfnIfrFqXPsGDDl3rYLkwh1O4/hYQ
NKNW9KTxoJxuBmAkm7EXQQh1XUUzajdEDqDBVRyvR0Z2MdMYnMSAiiMXMl2wUZnc
QXYftBo0HbtfsaJjImQdDjmlmRPSzE/RW6iUe+1cesKBC7e8nVf69Yu/fxO4m083
VWwAstlWJfk1GyU7jzVc8svealg/oIiDoOMe6CFSLx1BDv2FeHSpRdqd3fn+AC73
bK2N2smrHUOQnFijuiFw3WOrjERi0eMhjVNfVu9W9ZYa/Wd6SdIzV55LbG+NpmSf
5W7ix41hRvdT6cTAJA==
-----END CERTIFICATE-----
            """.trimIndent(),
        )
    }

    /**
     * Query a single ElectrumX server for a Namecoin name.
     *
     * @throws NamecoinLookupException for definitive blockchain answers
     * @throws Exception for network/IO errors
     */
    suspend fun nameShow(
        identifier: String,
        server: ElectrumxServer = DEFAULT_SERVERS.first(),
    ): NameShowResult = withContext(Dispatchers.IO) {
        val serverKey = "${server.host}:${server.port}"
        val serverMutex = serverMutexes.getOrPut(serverKey) { Mutex() }
        serverMutex.withLock {
            connectAndQuery(identifier, server)
        }
    }

    suspend fun nameShowWithFallback(
        identifier: String,
        servers: List<ElectrumxServer> = DEFAULT_SERVERS,
    ): NameShowResult {
        var lastException: Exception? = null
        for (server in servers) {
            try {
                return nameShow(identifier, server)
            } catch (e: NamecoinLookupException.NameNotFound) {
                throw e // Definitive: name doesn't exist
            } catch (e: NamecoinLookupException.NameExpired) {
                throw e // Definitive: name expired
            } catch (e: NamecoinLookupException.NoNostrKey) {
                throw e // Definitive: name has no Nostr key
            } catch (e: NamecoinLookupException.ParseError) {
                lastException = e
                continue // Might be server-specific — try next
            } catch (e: Exception) {
                lastException = e
                continue // Network error — try next server
            }
        }
        throw NamecoinLookupException.ServersUnreachable(
            "All ${servers.size} servers failed. Last error: ${lastException?.message}",
        )
    }

    /**
     * Test connectivity to a single ElectrumX server.
     *
     * Connects, negotiates protocol version, and optionally resolves a test
     * name. Returns detailed results including response time, TLS version,
     * and human-readable error messages.
     */
    suspend fun testServer(
        server: ElectrumxServer,
        testName: String? = "d/testls",
    ): ServerTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val socket = createSocket(server)
            socket.soTimeout = readTimeoutMs.toInt()

            var tlsVersion: String? = null
            var serverCertPem: String? = null
            var certFingerprint: String? = null

            if (socket is SSLSocket) {
                tlsVersion = socket.session.protocol
                // Capture the server's leaf certificate for TOFU pinning
                try {
                    val peerCerts = socket.session.peerCertificates
                    if (peerCerts.isNotEmpty() && peerCerts[0] is X509Certificate) {
                        val x509 = peerCerts[0] as X509Certificate
                        val encoded = Base64.getMimeEncoder(76, "\n".toByteArray())
                            .encodeToString(x509.encoded)
                        serverCertPem = "-----BEGIN CERTIFICATE-----\n$encoded-----END CERTIFICATE-----"
                        val digest = MessageDigest.getInstance("SHA-256").digest(x509.encoded)
                        certFingerprint = digest.joinToString(":") { "%02X".format(it) }
                    }
                } catch (_: Exception) {
                    // Non-fatal — cert capture is best-effort
                }
            }

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            try {
                val versionReq = buildRpcRequest("server.version", listOf("PrimalNMC/0.1", PROTOCOL_VERSION))
                writer.println(versionReq)
                val versionResponse = reader.readLine()
                    ?: return@withContext ServerTestResult(
                        server = server,
                        success = false,
                        responseTimeMs = System.currentTimeMillis() - startTime,
                        error = "Server returned empty response",
                        tlsVersion = tlsVersion,
                    )

                if (testName != null) {
                    val nameScript = buildNameIndexScript(testName.toByteArray(Charsets.US_ASCII))
                    val scriptHash = electrumScriptHash(nameScript)
                    val historyReq = buildRpcRequest("blockchain.scripthash.get_history", listOf(scriptHash))
                    writer.println(historyReq)
                    reader.readLine()
                }

                val elapsed = System.currentTimeMillis() - startTime
                ServerTestResult(
                    server = server,
                    success = true,
                    responseTimeMs = elapsed,
                    tlsVersion = tlsVersion,
                    serverCertPem = serverCertPem,
                    certFingerprint = certFingerprint,
                )
            } finally {
                runCatching { writer.close() }
                runCatching { reader.close() }
                runCatching { socket.close() }
            }
        } catch (e: java.net.ConnectException) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = "Connection refused",
            )
        } catch (e: java.net.SocketTimeoutException) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = "Connection timed out after ${connectTimeoutMs / 1000}s",
            )
        } catch (e: java.net.UnknownHostException) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = "Server unreachable (DNS resolution failed)",
            )
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            val detail = if (e.message?.contains("self-signed", ignoreCase = true) == true ||
                e.message?.contains("anchor", ignoreCase = true) == true
            ) {
                "TLS handshake failed (self-signed certificate rejected)"
            } else {
                "TLS handshake failed: ${e.message?.take(100) ?: "unknown error"}"
            }
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = detail,
            )
        } catch (e: javax.net.ssl.SSLException) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = "TLS error: ${e.message?.take(100) ?: "unknown"}",
            )
        } catch (e: java.io.IOException) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = "I/O error: ${e.message?.take(100) ?: "unknown"}",
            )
        } catch (e: Exception) {
            ServerTestResult(
                server = server,
                success = false,
                responseTimeMs = System.currentTimeMillis() - startTime,
                error = e.message?.take(150) ?: "Unknown error",
            )
        }
    }

    // ── TOFU cert management ───────────────────────────────────────────

    /**
     * Add a PEM-encoded certificate to the dynamic trust store.
     * Typically called after the user confirms a cert fingerprint via
     * the "Test Connection" flow in settings.
     *
     * Invalidates the cached factory so the next connection picks it up.
     */
    fun addPinnedCert(pem: String) {
        synchronized(this) {
            dynamicCerts.add(pem)
            pinnedFactory = null // force rebuild
        }
    }

    /**
     * Replace all dynamic certs (e.g. loaded from preferences on startup).
     */
    fun setDynamicCerts(pems: List<String>) {
        synchronized(this) {
            dynamicCerts.clear()
            dynamicCerts.addAll(pems)
            pinnedFactory = null
        }
    }

    // ── internals ──────────────────────────────────────────────────────

    /**
     * Connect to a single server and query for a Namecoin name.
     *
     * @throws NamecoinLookupException.NameNotFound if the name has no history on-chain
     * @throws NamecoinLookupException.NameExpired if the name has expired
     * @throws NamecoinLookupException.NoNostrKey if the name exists but has no Nostr pubkey
     * @throws NamecoinLookupException.ParseError if the server response is malformed
     * @throws Exception for network/IO errors (caller should try next server)
     */
    private fun connectAndQuery(
        identifier: String,
        server: ElectrumxServer,
    ): NameShowResult {
        val socket = createSocket(server)
        socket.soTimeout = readTimeoutMs.toInt()
        val writer = PrintWriter(socket.getOutputStream(), true)
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        try {
            val versionReq = buildRpcRequest("server.version", listOf("PrimalNMC/0.1", PROTOCOL_VERSION))
            writer.println(versionReq)
            reader.readLine()

            val nameScript = buildNameIndexScript(identifier.toByteArray(Charsets.US_ASCII))
            val scriptHash = electrumScriptHash(nameScript)

            val historyReq = buildRpcRequest("blockchain.scripthash.get_history", listOf(scriptHash))
            writer.println(historyReq)
            val historyResponse = reader.readLine()
                ?: throw NamecoinLookupException.ParseError(identifier)
            val historyEntries = parseHistoryResponse(historyResponse)
                ?: throw NamecoinLookupException.ParseError(identifier)
            if (historyEntries.isEmpty()) {
                throw NamecoinLookupException.NameNotFound(identifier)
            }

            val latestEntry = historyEntries.last()
            val txHash = latestEntry.first
            val height = latestEntry.second

            val txReq = buildRpcRequest("blockchain.transaction.get", listOf(txHash, true))
            writer.println(txReq)
            val txResponse = reader.readLine()
                ?: throw NamecoinLookupException.ParseError(identifier)

            val headersReq = buildRpcRequest("blockchain.headers.subscribe", emptyList<String>())
            writer.println(headersReq)
            val headersResponse = reader.readLine()
            val currentHeight = parseBlockHeight(headersResponse)

            if (currentHeight != null && height > 0) {
                val blocksSince = currentHeight - height
                if (blocksSince >= NAME_EXPIRE_DEPTH) {
                    throw NamecoinLookupException.NameExpired(identifier, blocksSince)
                }
            }

            val result = parseNameFromTransaction(identifier, txHash, height, txResponse)
                ?: throw NamecoinLookupException.NoNostrKey(identifier)

            return if (currentHeight != null && height > 0) {
                result.copy(expiresIn = NAME_EXPIRE_DEPTH - (currentHeight - height))
            } else {
                result
            }
        } finally {
            runCatching { writer.close() }
            runCatching { reader.close() }
            runCatching { socket.close() }
        }
    }

    private fun buildNameIndexScript(nameBytes: ByteArray): ByteArray {
        val result = mutableListOf<Byte>()
        result.add(OP_NAME_UPDATE)
        result.addAll(pushData(nameBytes).toList())
        result.addAll(pushData(byteArrayOf()).toList())
        result.add(OP_2DROP)
        result.add(OP_DROP)
        result.add(OP_RETURN)
        return result.toByteArray()
    }

    private fun pushData(data: ByteArray): ByteArray {
        val len = data.size
        return when {
            len < 0x4c -> byteArrayOf(len.toByte()) + data
            len <= 0xff -> byteArrayOf(OP_PUSHDATA1, len.toByte()) + data
            else -> {
                val lenBytes = byteArrayOf((len and 0xff).toByte(), ((len shr 8) and 0xff).toByte())
                byteArrayOf(OP_PUSHDATA2) + lenBytes + data
            }
        }
    }

    private fun electrumScriptHash(script: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(script)
        return digest.reversedArray().joinToString("") { "%02x".format(it) }
    }

    private fun parseBlockHeight(raw: String?): Int? {
        if (raw == null) return null
        return try {
            val envelope = json.parseToJsonElement(raw).jsonObject
            envelope["result"]?.jsonObject?.get("height")?.jsonPrimitive?.int
        } catch (_: Exception) {
            null
        }
    }

    private fun parseHistoryResponse(raw: String): List<Pair<String, Int>>? {
        val envelope = json.parseToJsonElement(raw).jsonObject
        val error = envelope["error"]
        if (error != null && error !is kotlinx.serialization.json.JsonNull) return null

        val result = envelope["result"]?.jsonArray ?: return null
        return result.mapNotNull { entry ->
            val obj = entry.jsonObject
            val txHash = obj["tx_hash"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val height = obj["height"]?.jsonPrimitive?.int ?: return@mapNotNull null
            txHash to height
        }
    }

    private fun parseNameFromTransaction(
        identifier: String,
        txHash: String,
        height: Int,
        raw: String,
    ): NameShowResult? {
        val envelope = json.parseToJsonElement(raw).jsonObject
        val error = envelope["error"]
        if (error != null && error !is kotlinx.serialization.json.JsonNull) return null

        val result = envelope["result"]?.jsonObject ?: return null
        val vouts = result["vout"]?.jsonArray ?: return null

        for (vout in vouts) {
            val scriptHex = vout.jsonObject["scriptPubKey"]
                ?.jsonObject?.get("hex")?.jsonPrimitive?.content ?: continue
            if (!scriptHex.startsWith("53")) continue

            val scriptBytes = hexToBytes(scriptHex)
            val parsed = parseNameScript(scriptBytes) ?: continue
            if (parsed.first == identifier) {
                return NameShowResult(
                    name = parsed.first,
                    value = parsed.second,
                    txid = txHash,
                    height = height,
                )
            }
        }
        return null
    }

    private fun parseNameScript(script: ByteArray): Pair<String, String>? {
        if (script.isEmpty() || script[0] != OP_NAME_UPDATE) return null
        var pos = 1
        val (nameBytes, newPos1) = readPushData(script, pos) ?: return null
        pos = newPos1
        val (valueBytes, _) = readPushData(script, pos) ?: return null
        return String(nameBytes, Charsets.US_ASCII) to String(valueBytes, Charsets.UTF_8)
    }

    private fun readPushData(script: ByteArray, pos: Int): Pair<ByteArray, Int>? {
        if (pos >= script.size) return null
        val opcode = script[pos].toInt() and 0xff
        return when {
            opcode == 0 -> byteArrayOf() to (pos + 1)
            opcode < 0x4c -> {
                val end = pos + 1 + opcode
                if (end > script.size) return null
                script.copyOfRange(pos + 1, end) to end
            }
            opcode == 0x4c -> {
                if (pos + 2 > script.size) return null
                val len = script[pos + 1].toInt() and 0xff
                val end = pos + 2 + len
                if (end > script.size) return null
                script.copyOfRange(pos + 2, end) to end
            }
            opcode == 0x4d -> {
                if (pos + 3 > script.size) return null
                val len = (script[pos + 1].toInt() and 0xff) or ((script[pos + 2].toInt() and 0xff) shl 8)
                val end = pos + 3 + len
                if (end > script.size) return null
                script.copyOfRange(pos + 3, end) to end
            }
            else -> null
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }

    private fun createSocket(server: ElectrumxServer): Socket {
        val baseSocket = socketFactory().createSocket().apply {
            connect(InetSocketAddress(server.host, server.port), connectTimeoutMs.toInt())
        }
        if (!server.useSsl) return baseSocket

        // Upgrade to TLS over the already-connected (possibly proxied) socket.
        // When the server uses a self-signed certificate (trustAllCerts flag),
        // we use a pinned trust store that contains the known ElectrumX server
        // certs. This is required because Samsung One UI 7 (Android 16) silently
        // rejects connections that use a no-op "trust-all" X509TrustManager.
        //
        // Exception: .onion addresses bypass cert pinning entirely — the Tor
        // hidden service protocol already provides end-to-end authentication
        // via the onion address, making TLS cert verification redundant.
        val sslFactory = if (server.host.endsWith(".onion")) {
            onionSslFactory()
        } else if (server.trustAllCerts) {
            cachedPinnedSslFactory()
        } else {
            SSLSocketFactory.getDefault() as SSLSocketFactory
        }
        val sslSocket = sslFactory.createSocket(baseSocket, server.host, server.port, true)

        // Enforce TLSv1.2+ — some OEM Conscrypt forks (Xiaomi MIUI, OnePlus ColorOS)
        // may negotiate TLS 1.0/1.1 by default for raw socket upgrades.
        if (sslSocket is SSLSocket) {
            val supported = sslSocket.supportedProtocols
            val modern = supported.filter { it == "TLSv1.2" || it == "TLSv1.3" }
            if (modern.isNotEmpty()) {
                sslSocket.enabledProtocols = modern.toTypedArray()
            }
        }

        return sslSocket
    }

    /**
     * SSLSocketFactory for .onion addresses.
     *
     * Tor hidden services are authenticated by their onion address (the
     * public key hash), so TLS certificate verification is redundant.
     * We use a trust-all factory here — this is safe because:
     * 1. The connection is already end-to-end encrypted by Tor.
     * 2. The onion address IS the server's identity proof.
     */
    private fun onionSslFactory(): SSLSocketFactory {
        val trustAll = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            },
        )
        val ctx = try {
            SSLContext.getInstance("TLSv1.2")
        } catch (_: Exception) {
            SSLContext.getInstance("TLS")
        }
        ctx.init(null, trustAll, SecureRandom())
        return ctx.socketFactory
    }

    private fun cachedPinnedSslFactory(): SSLSocketFactory {
        pinnedFactory?.let { return it }
        synchronized(this) {
            pinnedFactory?.let { return it }
            return buildPinnedSslFactory().also { pinnedFactory = it }
        }
    }

    /**
     * Build an SSLSocketFactory that trusts the pinned ElectrumX server
     * certificates plus the system CA store.
     *
     * Previous versions used a "trust-all" TrustManager, but Samsung
     * devices running One UI 7 (Android 16) silently reject connections
     * that use a no-op X509TrustManager. Pinning the known self-signed
     * certs avoids this while maintaining security.
     *
     * Also handles OEM-specific quirks:
     * - Xiaomi MIUI/HyperOS: KeyStore.getDefaultType() may return unexpected
     *   types; we try the default first, then fall back to "PKCS12".
     * - OnePlus ColorOS: some versions require explicit TLSv1.2 protocol.
     * - All OEMs: SSLContext("TLSv1.2") is preferred over ("TLS") which may
     *   resolve to TLS 1.0 on older Conscrypt forks.
     */
    private fun buildPinnedSslFactory(): SSLSocketFactory {
        val ks = try {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        } catch (_: Exception) {
            // Fallback for Xiaomi devices where getDefaultType() returns an unsupported type
            KeyStore.getInstance("PKCS12").apply { load(null, null) }
        }

        val cf = CertificateFactory.getInstance("X.509")

        // Load hardcoded + dynamic pinned certificates into the keystore
        val allCerts = PINNED_ELECTRUMX_CERTS + dynamicCerts
        for ((index, pem) in allCerts.withIndex()) {
            try {
                val cert = cf.generateCertificate(ByteArrayInputStream(pem.toByteArray(Charsets.US_ASCII)))
                ks.setCertificateEntry("electrumx_$index", cert)
            } catch (_: Exception) {
                // Skip malformed certs — the remaining ones may still work
            }
        }

        // Also load system CA certificates so that servers with real certs work too
        val systemTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        systemTmf.init(null as KeyStore?) // null = system default
        val systemTm = systemTmf.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
        if (systemTm != null) {
            for ((index, issuer) in systemTm.acceptedIssuers.withIndex()) {
                try {
                    ks.setCertificateEntry("system_$index", issuer)
                } catch (_: Exception) {
                    // Some OEMs return certs that can't be re-inserted; skip
                }
            }
        }

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        // Prefer TLSv1.2 explicitly — SSLContext.getInstance("TLS") can resolve
        // to TLS 1.0 on some OEM Conscrypt forks (Xiaomi, OnePlus).
        val sslContext = try {
            SSLContext.getInstance("TLSv1.2")
        } catch (_: Exception) {
            SSLContext.getInstance("TLS")
        }
        sslContext.init(null, tmf.trustManagers, SecureRandom())
        return sslContext.socketFactory
    }

    private fun buildRpcRequest(method: String, params: List<Any>): String {
        val id = requestId.incrementAndGet()
        val obj = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", id)
            put("method", method)
            put(
                "params",
                json.encodeToJsonElement(
                    kotlinx.serialization.builtins.ListSerializer(
                        kotlinx.serialization.json.JsonElement.serializer(),
                    ),
                    params.map {
                        when (it) {
                            is Boolean -> JsonPrimitive(it)
                            is Number -> JsonPrimitive(it)
                            else -> JsonPrimitive(it.toString())
                        }
                    },
                ),
            )
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }
}
