@file:Suppress("TooManyFunctions")
package net.primal.android.premium.utils

import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.user.domain.UserAccount

fun String?.isPremiumTier() = this == "premium"
fun String?.isPremiumFreeTier() = this == "free"
fun String?.isPrimalLegendTier() = this == "premium-legend"

fun PremiumMembership?.isPremiumTier() = this?.tier.isPremiumTier()
fun PremiumMembership?.isPremiumFreeTier() = this?.tier.isPremiumFreeTier()
fun PremiumMembership?.isPrimalLegendTier() = this?.tier.isPrimalLegendTier()

fun String?.isOriginAndroid() = this?.lowercase() == "android"
fun String?.isOriginIOS() = this?.lowercase() == "ios"
fun String?.isOriginWeb() = this?.lowercase() == "web"

fun PremiumMembership.hasPremiumMembership() = !this.isExpired()
fun UserAccount.hasPremiumMembership() = this.premiumMembership?.hasPremiumMembership() == true
