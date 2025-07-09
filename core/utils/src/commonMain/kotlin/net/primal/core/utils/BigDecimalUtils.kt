package net.primal.core.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

fun BigDecimal.safeDivide(other: BigDecimal) =
    divide(
        other = other,
        decimalMode = DecimalMode(decimalPrecision = 30, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO),
    )

fun BigDecimal.toLong() = this.longValue(exactRequired = false)
fun BigDecimal.toULong() = this.ulongValue(exactRequired = false)
fun BigDecimal.toDouble() = this.doubleValue(exactRequired = false)
fun BigDecimal.toInt() = this.intValue(exactRequired = false)
fun BigDecimal.toFloat() = this.floatValue(exactRequired = false)
