package net.primal.android.wallet.store.domain

data class SubscriptionProduct(
    val productId: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val billingPeriod: SubscriptionBillingPeriod,
)

enum class SubscriptionBillingPeriod {
    Monthly,
    Yearly,
}
