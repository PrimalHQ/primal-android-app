package net.primal.android.wallet.utils

@SuppressWarnings("ImplicitDefaultLocale")
object ZapConversionUtils {
    private const val BTC_IN_SATS = 100_000_000.00

    fun ULong.toBtc() = this.toDouble() / BTC_IN_SATS

    fun Int.toBtc() = this.toULong().toBtc()

    fun Long.toBtc() = this.toULong().toBtc()

    fun Double.toSats(): ULong = (this * BTC_IN_SATS).toULong()

    fun Double.formatAsString() = String.format("%.11f", this).trimEnd('0').trimEnd('.')
}
