package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LegendPaymentInstructionsResponse(
    @SerialName("membership_quote_id") val membershipQuoteId: String,
    @SerialName("amount_usd") val amountUsd: String,
    @SerialName("amount_btc") val amountBtc: String,
    @SerialName("qr_code") val qrCode: String,
)
