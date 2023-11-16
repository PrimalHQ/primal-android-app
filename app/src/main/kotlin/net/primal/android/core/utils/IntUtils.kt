package net.primal.android.core.utils

fun Int.shortened(): String = toLong().shortened()

fun ULong.shortened(): String = toLong().shortened()

fun Long.shortened(): String {
    if (this < 1000) {
        return this.toString()
    }

    val multipliers = listOf(
        Pair("K", 1000.0),
        Pair("M", 1000000.0),
        Pair("B", 1000000000.0),
    )

    for ((shorten, multiplier) in multipliers) {
        val total = this.toDouble() / multiplier
        if (total < 10) {
            val formatted = String.format("%.1f", total)
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
