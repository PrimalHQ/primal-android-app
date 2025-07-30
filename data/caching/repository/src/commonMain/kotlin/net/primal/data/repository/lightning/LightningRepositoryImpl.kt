package net.primal.data.repository.lightning

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.remote.api.lightning.LightningApi
import net.primal.domain.nostr.lightning.LightningRepository
import net.primal.domain.nostr.lightning.model.InvoiceResponse
import net.primal.domain.nostr.lightning.model.PayRequest
import net.primal.domain.nostr.utils.decodeLNUrlOrNull

class LightningRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val lightningApi: LightningApi,
) : LightningRepository {
    override suspend fun getPayRequest(lnUrl: String): Result<PayRequest> =
        withContext(dispatcherProvider.io()) {
            val decodedLnUrl = if (lnUrl.startsWith("http://") || lnUrl.startsWith("https://")) {
                lnUrl
            } else {
                lnUrl.removePrefix("lightning:").decodeLNUrlOrNull()
            } ?: return@withContext Result.failure(IllegalArgumentException("Couldn't decode '$lnUrl'."))

            runCatching { lightningApi.getPayRequest(lnUrl = decodedLnUrl) }
        }

    override suspend fun getInvoice(
        callbackUrl: String,
        amountMSats: Long,
        comment: String?,
    ): Result<InvoiceResponse> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                lightningApi.getInvoice(
                    callbackUrl = callbackUrl,
                    amountMSats = amountMSats,
                    comment = comment,
                )
            }
        }
}
