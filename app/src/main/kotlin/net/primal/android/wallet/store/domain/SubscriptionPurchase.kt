package net.primal.android.wallet.store.domain

data class SubscriptionPurchase(
    val orderId: String?,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    val purchaseToken: String,
    val quantity: Int,
    val autoRenewing: Boolean,
    val playSubscriptionJson: String,
)
