package net.primal.core.lightning.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Url
import net.primal.domain.lightning.model.InvoiceResponse
import net.primal.domain.lightning.model.PayRequest

interface LightningApi {
    @GET
    suspend fun getPayRequest(@Url lnUrl: String): PayRequest

    @GET
    suspend fun getInvoice(
        @Url callbackUrl: String,
        @Query("amount") amountMSats: Long,
        @Query comment: String?,
    ): InvoiceResponse
}
