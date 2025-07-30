package net.primal.core.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

@Suppress("TooManyFunctions")
object CurrencyConversionUtils {
    private const val BTC_IN_SATS = 100_000_000L
    private const val BTC_IN_MSATS = 100_000_000_000L
    private const val SATS_IN_MSATS = 1000L

    fun ULong.toBtc() = this.toDouble() / BTC_IN_SATS

    fun Long.msatsToBtc() = this.toDouble() / BTC_IN_MSATS

    fun Double.btcToMSats() = this * BTC_IN_MSATS

    fun Long.satsToMSats() = this * SATS_IN_MSATS

    fun Int.toBtc() = this.toULong().toBtc()

    fun Long.toBtc() = this.toULong().toBtc()

    fun BigDecimal.toBtc() = this.div(BTC_IN_SATS)

    fun Double.toSats() = this * BTC_IN_SATS

    fun ULong.toSats() = this.toDouble().toSats()

    fun Int.toSats() = this.toDouble().toSats()

    fun Long.toSats() = this.toDouble().toSats()

    fun String.toSats(): ULong = this.toBigDecimal().toSats()

    fun BigDecimal.toSats(): ULong = multiply(BTC_IN_SATS.toBigDecimal()).toULong()

    fun BigDecimal.toUsd(exchangeBtcUsdRate: Double?): BigDecimal {
        val rate = exchangeBtcUsdRate ?: 0.0
        return multiply(rate.toBigDecimal()).scale(2)
    }

    fun BigDecimal.fromSatsToUsd(exchangeBtcUsdRate: Double?): BigDecimal {
        val rate = exchangeBtcUsdRate ?: 0.0
        val btcAmount = this.toBtc()
        return btcAmount.multiply(rate.toBigDecimal()).scale(2)
    }

    fun BigDecimal.fromUsdToSats(exchangeBtcUsdRate: Double?): ULong {
        val rate = exchangeBtcUsdRate ?: 0.0
        val btcAmount = safeDivide(other = rate.toBigDecimal())
        return btcAmount.toSats()
    }

    fun Double.formatAsString() = this.toBigDecimal().toStringExpanded()

    fun String.toBigDecimal() = BigDecimal.parseString(this)
}
