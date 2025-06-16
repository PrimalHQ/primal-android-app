package net.primal.android.premium.buying.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import net.primal.android.R
import net.primal.android.premium.buying.home.PREMIUM_PINK
import net.primal.android.premium.buying.utils.calculateDiscountPercent
import net.primal.android.premium.ui.toPricingString
import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionTier

@Composable
fun PrimalPremiumOfferCard(
    loading: Boolean,
    premiumSubscriptions: List<SubscriptionProduct>,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
    paddingValues: PaddingValues,
    priceFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val premiumMonthly = remember(premiumSubscriptions) {
        premiumSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Monthly }
    }
    val premiumYearly = remember(premiumSubscriptions) {
        premiumSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Yearly }
    }

    val premiumDiscountPercent = remember(premiumMonthly, premiumYearly) {
        calculateDiscountPercent(premiumMonthly, premiumYearly)
    }

    val premiumBadgeText = if (!loading) {
        premiumDiscountPercent?.let {
            stringResource(R.string.subscription_save_percent, it)
        }
    } else {
        null
    }

    OfferCard(
        modifier = modifier,
        paddingValues = paddingValues,
        titleSuffix = stringResource(R.string.subscription_primal_premium_title),
        titleColor = PREMIUM_PINK,
        priceText = if (loading) "---" else premiumMonthly?.toPricingString() ?: "---",
        billingText = stringResource(
            R.string.subscription_billed_annually,
            if (loading) "---" else premiumYearly?.toPricingString() ?: "---",
        ),

        badgeColor = PREMIUM_PINK,
        descriptionItems = listOf(
            stringResource(R.string.subscription_primal_premium_perk_verified_nostr_address),
            stringResource(R.string.subscription_primal_premium_perk_custom_lightning_address),
            stringResource(R.string.subscription_primal_premium_perk_vip_profile),
            stringResource(R.string.subscription_primal_premium_perk_advanced_nostr_search),
            stringResource(R.string.subscription_primal_premium_perk_paid_relay),
            stringResource(R.string.subscription_primal_premium_perk_10gb_media_storage),
            stringResource(R.string.subscription_primal_premium_perk_1gb_max_file_size),
        ),
        buttonText = stringResource(R.string.subscription_primal_buy_premium),
        onBuyOfferClick = { onPurchaseSubscription(SubscriptionTier.PREMIUM) },
        autoSizePrice = false,
        priceFontSize = priceFontSize,
        badgeText = premiumBadgeText,
    )
}
