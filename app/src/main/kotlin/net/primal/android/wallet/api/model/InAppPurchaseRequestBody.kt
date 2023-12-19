package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppPurchaseRequestBody(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("transaction_id") val purchaseToken: String,
) : WalletOperationRequestBody()
