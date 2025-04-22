package net.primal.core.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal

fun BigDecimal.toLong() = this.longValue(exactRequired = false)
fun BigDecimal.toULong() = this.ulongValue(exactRequired = false)
fun BigDecimal.toDouble() = this.doubleValue(exactRequired = false)
fun BigDecimal.toInt() = this.intValue(exactRequired = false)
fun BigDecimal.toFloat() = this.floatValue(exactRequired = false)
