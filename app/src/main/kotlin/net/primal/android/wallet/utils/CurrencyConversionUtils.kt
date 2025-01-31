package net.primal.android.wallet.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

@SuppressWarnings("ImplicitDefaultLocale")
object CurrencyConversionUtils {
    private const val BTC_IN_SATS = 100_000_000.00

    fun ULong.toBtc() = this.toDouble() / BTC_IN_SATS

    fun Int.toBtc() = this.toULong().toBtc()

    fun Long.toBtc() = this.toULong().toBtc()

    fun BigDecimal.toBtc() = this.toLong().toBtc()

    fun Double.toSats() = this * BTC_IN_SATS

    fun ULong.toSats() = this.toDouble().toSats()

    fun Int.toSats() = this.toDouble().toSats()

    fun Long.toSats() = this.toDouble().toSats()

    fun String.toSats(): ULong = this.toBigDecimal().toSats()

    fun BigDecimal.toSats(): ULong = multiply(BTC_IN_SATS.toBigDecimal()).toLong().toULong()

    fun BigDecimal.toUsd(exchangeBtcUsdRate: Double?): BigDecimal {
        val rate = exchangeBtcUsdRate ?: 0.0
        return multiply(BigDecimal(rate)).setScale(2, RoundingMode.HALF_EVEN)
    }

    fun BigDecimal.fromSatsToUsd(exchangeBtcUsdRate: Double?): BigDecimal {
        val rate = exchangeBtcUsdRate ?: 0.0
        val btcAmount = this.toBtc()
        return BigDecimal(btcAmount).multiply(BigDecimal(rate)).setScale(2, RoundingMode.HALF_EVEN)
    }

    fun BigDecimal.fromUsdToSats(exchangeBtcUsdRate: Double?): ULong {
        val rate = exchangeBtcUsdRate ?: 0.0
        val btcAmount = this.divide(BigDecimal(rate), MathContext.DECIMAL128)
        return btcAmount.toSats()
    }

    fun Double.formatAsString() = String.format(Locale.US, "%.11f", this).trimEnd('0').trimEnd('.')
}
