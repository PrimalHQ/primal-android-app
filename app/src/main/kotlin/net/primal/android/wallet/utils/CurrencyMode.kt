package net.primal.android.wallet.utils

enum class CurrencyMode {
    FIAT,
    SATS,
}

operator fun CurrencyMode.not(): CurrencyMode {
    return when (this) {
        CurrencyMode.FIAT -> CurrencyMode.SATS
        CurrencyMode.SATS -> CurrencyMode.FIAT
    }
}
