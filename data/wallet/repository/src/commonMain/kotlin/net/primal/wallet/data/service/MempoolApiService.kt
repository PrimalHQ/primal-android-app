package net.primal.wallet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull

internal class MempoolApiService(
    private val httpClient: HttpClient = HttpClientFactory.createHttpClientWithDefaultConfig(),
    private val baseUrl: String = BASE_URL,
) {

    suspend fun getUnconfirmedTransactions(address: String): List<MempoolTransaction> {
        val body = httpClient.get("$baseUrl/api/address/$address/txs/mempool").bodyAsText()
        return body.decodeFromJsonStringOrNull<List<MempoolTransaction>>() ?: emptyList()
    }

    suspend fun getTransaction(txid: String): MempoolTransaction? {
        val body = httpClient.get("$baseUrl/api/tx/$txid").bodyAsText()
        return body.decodeFromJsonStringOrNull<MempoolTransaction>()
    }

    suspend fun lookupTransaction(txid: String): TransactionLookupResult {
        val response = httpClient.get("$baseUrl/api/tx/$txid")
        if (response.status == HttpStatusCode.NotFound) {
            return TransactionLookupResult.NotFound
        }
        val tx = response.bodyAsText().decodeFromJsonStringOrNull<MempoolTransaction>()
            ?: return TransactionLookupResult.Error
        return TransactionLookupResult.Found(tx)
    }

    private companion object {
        private const val BASE_URL = "https://mempool.space"
    }
}

@Serializable
internal data class MempoolTransaction(
    val txid: String,
    val vout: List<MempoolVout>,
    val status: MempoolTxStatus,
)

@Serializable
internal data class MempoolVout(
    val value: Long,
    @SerialName("scriptpubkey_address")
    val scriptpubkeyAddress: String? = null,
)

@Serializable
internal data class MempoolTxStatus(
    val confirmed: Boolean,
)

internal fun MempoolTransaction.totalReceivedSats(address: String): Long {
    return vout
        .filter { it.scriptpubkeyAddress == address }
        .sumOf { it.value }
}

internal fun MempoolTransaction.outputAddresses(): Set<String> {
    return vout.mapNotNullTo(mutableSetOf()) { it.scriptpubkeyAddress }
}

internal sealed interface TransactionLookupResult {
    data class Found(val transaction: MempoolTransaction) : TransactionLookupResult
    data object NotFound : TransactionLookupResult
    data object Error : TransactionLookupResult
}
