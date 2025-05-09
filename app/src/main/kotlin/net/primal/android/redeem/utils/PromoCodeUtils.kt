package net.primal.android.redeem.utils

private const val PROMO_CODE_URL_PREFIX = "https://primal.net/rc/"

fun String.isPromoCodeUrl() = startsWith(PROMO_CODE_URL_PREFIX)
fun String.getPromoCodeFromUrl() = removePrefix(PROMO_CODE_URL_PREFIX)
