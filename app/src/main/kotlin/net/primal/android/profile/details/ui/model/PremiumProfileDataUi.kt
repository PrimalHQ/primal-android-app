package net.primal.android.profile.details.ui.model

import kotlinx.datetime.Clock
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isPrimalLegendTier

data class PremiumProfileDataUi(
    val primalName: String? = null,
    val cohort1: String? = null,
    val cohort2: String? = null,
    val tier: String? = null,
    val expiresAt: Long? = null,
    val legendSince: Long? = null,
    val premiumSince: Long? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
)

fun PremiumProfileDataUi.shouldShowPremiumBadge(): Boolean {
    val legendaryStyle = legendaryCustomization?.legendaryStyle

    val showLegendBadge = tier.isPrimalLegendTier() && legendaryStyle != null &&
        legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION

    val showPremiumBadge = tier.isPremiumTier() && (expiresAt ?: 0L) > Clock.System.now().epochSeconds

    return (showLegendBadge || showPremiumBadge) && cohort1?.isNotEmpty() == true && cohort2?.isNotEmpty() == true
}
