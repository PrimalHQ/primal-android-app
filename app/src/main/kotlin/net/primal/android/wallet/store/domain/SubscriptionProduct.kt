package net.primal.android.wallet.store.domain

data class SubscriptionProduct(
    val productId: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val billingPeriod: SubscriptionBillingPeriod,
    val tier: SubscriptionTier,
)

enum class SubscriptionBillingPeriod {
    Monthly,
    Yearly,
}

enum class SubscriptionTier {
    PREMIUM,
    PRO,
}
