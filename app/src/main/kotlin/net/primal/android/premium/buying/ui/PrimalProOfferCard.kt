package net.primal.android.premium.buying.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import net.primal.android.R
import net.primal.android.premium.buying.home.PRO_ORANGE
import net.primal.android.premium.buying.utils.calculateDiscountPercent
import net.primal.android.premium.ui.toPricingString
import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionTier

@Composable
fun PrimalProOfferCard(
    loading: Boolean,
    proSubscriptions: List<SubscriptionProduct>,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
    paddingValues: PaddingValues,
    autoSizePrice: Boolean,
    priceFontSizeResolved: (TextUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val proMonthly = remember(proSubscriptions) {
        proSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Monthly }
    }
    val proYearly = remember(proSubscriptions) {
        proSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Yearly }
    }
    val proDiscountPercent = remember(proMonthly, proYearly) {
        calculateDiscountPercent(proMonthly, proYearly)
    }
    val proBadgeText = if (!loading) {
        proDiscountPercent?.let {
            stringResource(R.string.subscription_save_percent, it)
        }
    } else {
        null
    }

    OfferCard(
        modifier = modifier,
        paddingValues = paddingValues,
        titleSuffix = stringResource(R.string.subscription_primal_pro_title),
        titleColor = PRO_ORANGE,
        priceText = if (loading) "---" else proMonthly?.toPricingString() ?: "---",
        billingText = stringResource(
            R.string.subscription_billed_annually,
            if (loading) "---" else proYearly?.toPricingString() ?: "---",
        ),
        badgeColor = PRO_ORANGE,
        descriptionItems = listOf(
            stringResource(R.string.subscription_primal_pro_perk_everything_in_premium),
            stringResource(R.string.subscription_primal_pro_perk_studio),
            stringResource(R.string.subscription_primal_pro_perk_legend_status),
            stringResource(R.string.subscription_primal_pro_perk_100gb_storage),
            stringResource(R.string.subscription_primal_pro_perk_10gb_max_file),
        ),
        hideFirstBullet = true,
        buttonText = stringResource(R.string.subscription_primal_buy_pro),
        onBuyOfferClick = { onPurchaseSubscription(SubscriptionTier.PRO) },
        buttonColor = PRO_ORANGE,
        autoSizePrice = autoSizePrice,
        badgeText = proBadgeText,
        priceFontSizeResolved = priceFontSizeResolved,
    )
}
