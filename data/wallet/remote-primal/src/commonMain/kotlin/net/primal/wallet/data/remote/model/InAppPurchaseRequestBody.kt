package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppPurchaseRequestBody(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("transaction_id") val purchaseToken: String,
) : WalletOperationRequestBody()
