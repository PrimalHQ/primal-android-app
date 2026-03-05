package net.primal.core.utils

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

private const val SMALL_RANGE_THRESHOLD = 6
private const val INTERMEDIATE_CHIP_COUNT = 4

@Suppress("MagicNumber")
fun generateAmountChips(min: Long, max: Long): List<Long> {
    val effectiveMin = min.coerceAtLeast(1)
    val effectiveMax = max.coerceAtLeast(effectiveMin)
    val rangeSize = effectiveMax - effectiveMin + 1
    return when {
        effectiveMin == effectiveMax -> emptyList()
        rangeSize <= SMALL_RANGE_THRESHOLD -> (effectiveMin..effectiveMax).toList()
        else -> buildList {
            add(effectiveMin)
            val logMin = log10(effectiveMin.toDouble())
            val logMax = log10(effectiveMax.toDouble())
            val useLinear = logMax - logMin < 1.0
            for (i in 1..INTERMEDIATE_CHIP_COUNT) {
                val rawValue = if (useLinear) {
                    effectiveMin + i * (effectiveMax - effectiveMin) / (INTERMEDIATE_CHIP_COUNT + 1).toDouble()
                } else {
                    val logValue = logMin + i * (logMax - logMin) / (INTERMEDIATE_CHIP_COUNT + 1).toDouble()
                    10.0.pow(logValue)
                }
                val niceValue = roundToNiceNumber(rawValue).coerceIn(effectiveMin + 1, effectiveMax - 1)
                if (niceValue !in this) add(niceValue)
            }
            add(effectiveMax)
        }.distinct().sorted()
    }
}

@Suppress("MagicNumber")
fun roundToNiceNumber(value: Double): Long {
    if (value <= 0) return 1

    val exponent = log10(value).toInt()
    val base = 10.0.pow(exponent)
    val fraction = value / base

    val niceMultiplier = when {
        fraction < 1.5 -> 1.0
        fraction < 2.25 -> 2.0
        fraction < 3.75 -> 2.5
        fraction < 7.5 -> 5.0
        else -> 10.0
    }

    return (niceMultiplier * base).roundToLong()
}
