package net.primal.android.wallet.store.play

import com.android.billingclient.api.ProductDetails

data class InAppProduct(
    val productId: String,
    val title: String,
    val name: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val formattedPrice: String,
    val googlePlayProductDetails: ProductDetails,
)
