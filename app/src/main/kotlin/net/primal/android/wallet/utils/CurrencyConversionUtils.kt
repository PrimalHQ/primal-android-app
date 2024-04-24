package net.primal.android.wallet.utils

import java.math.BigDecimal
import java.util.*

@SuppressWarnings("ImplicitDefaultLocale")
object CurrencyConversionUtils {
    private const val BTC_IN_SATS = 100_000_000.00

    fun ULong.toBtc() = this.toDouble() / BTC_IN_SATS

    fun Int.toBtc() = this.toULong().toBtc()

    fun Long.toBtc() = this.toULong().toBtc()

    fun BigDecimal.toBtc() = this.toLong().toBtc()

    fun String.toSats(): ULong = this.toBigDecimal().toSats()

    fun BigDecimal.toSats(): ULong = multiply(BTC_IN_SATS.toBigDecimal()).toLong().toULong()

    fun Double.formatAsString() = String.format(Locale.US, "%.11f", this).trimEnd('0').trimEnd('.')
}
