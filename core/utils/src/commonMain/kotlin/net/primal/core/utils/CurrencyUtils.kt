package net.primal.core.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.CurrencyConversionUtils.toUsd

const val MAXIMUM_SATS = 99_999_990.00

fun getMaximumUsdAmount(exchangeRate: Double?): BigDecimal {
    return (MAXIMUM_SATS)
        .toBigDecimal()
        .toBtc()
        .toUsd(exchangeRate)
}
