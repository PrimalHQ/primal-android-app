package net.primal.android.wallet.store.domain

data class SubscriptionPurchase(
    val orderId: String?,
    val productId: String,
    val purchaseTime: Long,
    val purchaseToken: String,
    val quantity: Int,
)
