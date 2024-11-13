package net.primal.android.premium.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPurchaseOrder(
    @SerialName("purchased_at") val purchasedAt: Long,
    @SerialName("product_id") val productId: String,
    @SerialName("product_label") val productLabel: String,
    @SerialName("amount_btc") val amountBtc: String?,
    @SerialName("amount_usd") val amountUsd: String?,
    @SerialName("currency") val currency: String?,
)
