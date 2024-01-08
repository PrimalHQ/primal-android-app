package net.primal.android.wallet.store.domain

data class InAppProduct(
    val productId: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
)
