package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppPurchaseQuoteRequestBody(
    @SerialName("product_id") val productId: String,
    val region: String,
    val platform: String,
    @SerialName("quote_id") val quoteId: String? = null,
) : WalletOperationRequestBody()
