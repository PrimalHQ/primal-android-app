package net.primal.android.premium.utils

import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.user.domain.UserAccount

fun PremiumMembership?.isPremiumTier() = this?.tier == "premium"
fun PremiumMembership?.isPremiumFreeTier() = this?.tier == "free"
fun PremiumMembership?.isPrimalLegendTier() = this?.tier == "premium-legend"

fun String?.isOriginAndroid() = this?.lowercase() == "android"
fun String?.isOriginIOS() = this?.lowercase() == "ios"
fun String?.isOriginWeb() = this?.lowercase() == "web"

fun PremiumMembership.hasPremiumMembership() = !this.isExpired()
fun UserAccount.hasPremiumMembership() = this.premiumMembership?.hasPremiumMembership() == true
