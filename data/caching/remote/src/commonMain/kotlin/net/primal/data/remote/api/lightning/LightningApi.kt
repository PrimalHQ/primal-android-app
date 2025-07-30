package net.primal.data.remote.api.lightning

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Url
import net.primal.domain.nostr.lightning.model.InvoiceResponse
import net.primal.domain.nostr.lightning.model.PayRequest

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
