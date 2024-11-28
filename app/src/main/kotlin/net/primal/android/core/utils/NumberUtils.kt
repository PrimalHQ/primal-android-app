package net.primal.android.core.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
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

fun Long.toMegaBytes(): Float = (this / 1024f.pow(2))
fun Long.toGigaBytes(): Float = (this / 1024f.pow(3))

fun String.toFormattedNumberString(): String {
    val symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()
    val formatter = DecimalFormat("###,###", symbols)
    return formatter.format(this.toFloat())
}
