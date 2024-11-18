package net.primal.android.premium.utils

fun String?.isPrimalLegend() = this?.lowercase() == "primal legend"
fun String?.isPremiumFree() = this?.lowercase() == "free"
fun String?.isOriginAndroid() = this?.lowercase() == "android"
fun String?.isOriginIOS() = this?.lowercase() == "ios"
fun String?.isOriginWeb() = this?.lowercase() == "web"
