package net.primal.android.core.utils

import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

fun Int.shortened(): String = toLong().shortened()

@Suppress("MagicNumber", "ImplicitDefaultLocale")
fun Long.shortened(): String {
    if (this < 1000) {
        return this.toString()
    }

    val multipliers = listOf(
        "K" to 1000.0,
        "M" to 1000000.0,
        "B" to 1000000000.0,
    )

    for ((shorten, multiplier) in multipliers) {
        val total = this.toDouble() / multiplier
        if (total < 10) {
            val formatted = String.format(Locale.getDefault(), "%.1f", total)
            val splitString = formatted.split(".0")
            val string = if (splitString.size > 1) splitString[0] else formatted
            return "$string$shorten"
        }
        if (total < 1000) {
            return "${total.toInt()}$shorten"
        }
    }

    return "1T+"
}

fun Long.toMegaBytes(roundUp: Boolean = true): String = (this / 1024f.pow(2)).toStringWithTwoDecimals(roundUp)
fun Long.toGigaBytes(roundUp: Boolean = true): String = (this / 1024f.pow(3)).toStringWithTwoDecimals(roundUp)

fun Float.toStringWithTwoDecimals(roundUp: Boolean): String {
    val roundedValue = if (roundUp) {
        ceil(this * 100) / 100
    } else {
        floor(this * 100) / 100
    }
    return String.format(Locale.getDefault(), "%.2f", roundedValue)
}
