package net.primal.android.core.utils

import java.math.BigDecimal
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toUsd

const val MAXIMUM_SATS = 99_999_990.00

fun getMaximumUsdAmount(exchangeRate: Double?): BigDecimal {
    return (MAXIMUM_SATS)
        .toBigDecimal()
        .toBtc()
        .toBigDecimal()
        .toUsd(exchangeRate)
}
