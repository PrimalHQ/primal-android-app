package net.primal.android.premium.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct

@Composable
fun SubscriptionProduct.toBillingPeriodString(): String {
    return when (this.billingPeriod) {
        SubscriptionBillingPeriod.Yearly -> stringResource(id = R.string.premium_period_annually)
        SubscriptionBillingPeriod.Monthly -> stringResource(id = R.string.premium_period_monthly)
    }
}

@Suppress("MagicNumber")
@Composable
fun SubscriptionProduct.toPricingString(): String {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    return "${this.priceCurrencyCode} ${numberFormat.format(this.priceAmountMicros / 1_000_000.00)}"
}

@Composable
fun SubscriptionProduct.toGetSubscriptionString(): String {
    return when (this.billingPeriod) {
        SubscriptionBillingPeriod.Yearly -> stringResource(id = R.string.premium_purchase_get_annual_plan)
        SubscriptionBillingPeriod.Monthly -> stringResource(id = R.string.premium_purchase_get_monthly_plan)
    }
}
