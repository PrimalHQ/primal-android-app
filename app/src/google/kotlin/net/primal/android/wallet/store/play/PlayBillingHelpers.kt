package net.primal.android.wallet.store.play

fun String?.isProductIdProSubscription(): Boolean {
    return this.equals(PlayBillingClient.PRO_YEARLY_PRODUCT_ID) ||
        this.equals(PlayBillingClient.PRO_MONTHLY_PRODUCT_ID)
}

fun String?.isProductIdPremiumSubscription(): Boolean {
    return this.equals(PlayBillingClient.PREMIUM_YEARLY_PRODUCT_ID) ||
        this.equals(PlayBillingClient.PREMIUM_MONTHLY_PRODUCT_ID)
}
