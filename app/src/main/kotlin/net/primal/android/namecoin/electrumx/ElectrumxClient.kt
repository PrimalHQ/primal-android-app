/*
 * Namecoin ElectrumX client for NIP-05 identity verification.
 *
 * Ported from Amethyst PR #1734 by mstrofnone.
 * Original: https://github.com/vitorpamplona/amethyst/pull/1734
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.electrumx

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.Dispatchers
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
 * Lightweight ElectrumX client for Namecoin name resolution.
 *
 * Connects over TCP/TLS to a Namecoin ElectrumX server and resolves
 * Namecoin names to their current values using scripthash-based lookups.
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
    private val mutex = Mutex()

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
    }

    suspend fun nameShow(
        identifier: String,
        server: ElectrumxServer = DEFAULT_SERVERS.first(),
    ): NameShowResult? = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                connectAndQuery(identifier, server)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun nameShowWithFallback(
        identifier: String,
        servers: List<ElectrumxServer> = DEFAULT_SERVERS,
    ): NameShowResult? {
        for (server in servers) {
            val result = nameShow(identifier, server)
            if (result != null) return result
        }
        return null
    }

    // ── internals ──────────────────────────────────────────────────────

    private fun connectAndQuery(
        identifier: String,
        server: ElectrumxServer,
    ): NameShowResult? {
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
            val historyResponse = reader.readLine() ?: return null
            val historyEntries = parseHistoryResponse(historyResponse) ?: return null
            if (historyEntries.isEmpty()) return null

            val latestEntry = historyEntries.last()
            val txHash = latestEntry.first
            val height = latestEntry.second

            val txReq = buildRpcRequest("blockchain.transaction.get", listOf(txHash, true))
            writer.println(txReq)
            val txResponse = reader.readLine() ?: return null

            val headersReq = buildRpcRequest("blockchain.headers.subscribe", emptyList<String>())
            writer.println(headersReq)
            val headersResponse = reader.readLine()
            val currentHeight = parseBlockHeight(headersResponse)

            if (currentHeight != null && height > 0) {
                if (currentHeight - height >= NAME_EXPIRE_DEPTH) return null
            }

            val result = parseNameFromTransaction(identifier, txHash, height, txResponse)
            return if (result != null && currentHeight != null && height > 0) {
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
        val sslFactory = if (server.trustAllCerts) trustAllSslFactory()
        else SSLSocketFactory.getDefault() as SSLSocketFactory
        return sslFactory.createSocket(baseSocket, server.host, server.port, true)
    }

    private fun trustAllSslFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            },
        )
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
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
