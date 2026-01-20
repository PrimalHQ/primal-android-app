package net.primal.android.scan.utils

private const val PROMO_CODE_URL_PREFIX = "https://primal.net/rc/"
private const val PROMO_CODE_LENGTH = 8

fun String.isPromoCodeUrl() = startsWith(PROMO_CODE_URL_PREFIX)
fun String.getPromoCodeFromUrl() = removePrefix(PROMO_CODE_URL_PREFIX)

fun String.isValidPromoCode() = length == PROMO_CODE_LENGTH && all { it.isLetter() }
