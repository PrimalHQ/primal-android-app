package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppPurchaseQuoteResponse(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("amount_btc") val amountBtc: String,
    @SerialName("apple_user_currency") val inAppPurchaseCurrency: String,
    @SerialName("apple_user_currency_amount") val inAppPurchaseAmount: String,
)
