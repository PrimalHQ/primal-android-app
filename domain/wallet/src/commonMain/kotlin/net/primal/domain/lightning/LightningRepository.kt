package net.primal.domain.lightning

import net.primal.core.utils.Result
import net.primal.domain.lightning.model.InvoiceResponse
import net.primal.domain.lightning.model.PayRequest

interface LightningRepository {

    suspend fun getPayRequest(lnUrl: String): Result<PayRequest>

    suspend fun getInvoice(
        callbackUrl: String,
        amountMSats: Long,
        comment: String?,
    ): Result<InvoiceResponse>
}
