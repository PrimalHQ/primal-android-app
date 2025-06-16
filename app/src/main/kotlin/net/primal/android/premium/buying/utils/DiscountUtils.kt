package net.primal.android.premium.buying.utils

import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct

private const val MICROS_IN_UNIT = 1_000_000.0
private const val MONTHS_IN_YEAR = 12
private const val PERCENT_CONVERSION = 100

fun calculateDiscountPercent(monthly: SubscriptionProduct?, yearly: SubscriptionProduct?): Int? {
    val monthlyMicros = monthly?.takeIf { it.billingPeriod == SubscriptionBillingPeriod.Monthly }?.priceAmountMicros
    val yearlyMicros = yearly?.takeIf { it.billingPeriod == SubscriptionBillingPeriod.Yearly }?.priceAmountMicros

    if (monthlyMicros == null || yearlyMicros == null) return null

    val monthlyPrice = monthlyMicros / MICROS_IN_UNIT
    val yearlyPrice = yearlyMicros / MICROS_IN_UNIT

    val discount = if (monthlyPrice > 0.0 && yearlyPrice > 0.0) {
        ((1 - yearlyPrice / (monthlyPrice * MONTHS_IN_YEAR)) * PERCENT_CONVERSION).toInt()
    } else {
        null
    }

    return discount?.takeIf { it > 0 }
}
