package net.primal.android.core.ext

import kotlin.math.ln
import kotlin.math.pow

fun Int.toShorthandFormat(): String {
    if (this < 1000)
        return "" + this
    val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()
    return String.format("%.0f%c", this / 1000.0.pow(exp.toDouble()), "KMGTPE"[exp - 1])
}