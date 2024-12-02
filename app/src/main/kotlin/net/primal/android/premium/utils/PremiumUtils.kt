package net.primal.android.premium.utils

import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.user.domain.UserAccount

fun String?.isPrimalLegend() = this?.lowercase() == "primal legend"
fun String?.isPremiumFree() = this?.lowercase() == "free"
fun String?.isOriginAndroid() = this?.lowercase() == "android"
fun String?.isOriginIOS() = this?.lowercase() == "ios"
fun String?.isOriginWeb() = this?.lowercase() == "web"

fun PremiumMembership.hasPremiumMembership() = !this.isExpired()
fun UserAccount.hasPremiumMembership() = this.premiumMembership?.hasPremiumMembership() == true
