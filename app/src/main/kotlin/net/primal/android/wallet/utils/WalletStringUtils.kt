@file:Suppress("TooManyFunctions")

package net.primal.android.wallet.utils

import android.util.Patterns
import net.primal.android.navigation.asUrlDecoded
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.CurrencyConversionUtils.fromUsdToSats
import net.primal.core.utils.CurrencyConversionUtils.toBigDecimal
import org.bitcoinj.core.Address
import org.bitcoinj.params.MainNetParams

fun String.isLnInvoice() = startsWith(prefix = "lnbc", ignoreCase = true)

fun String.isLnUrl() = startsWith(prefix = "lnurl", ignoreCase = true)

fun String.isLightningAddress() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isLightningUri(): Boolean {
    val isPrefixCorrect = startsWith(prefix = "lightning:", ignoreCase = true)
    val path = this.split(":").last()
    val isPathCorrect = path.isLightningAddress() || path.isLnUrl() || path.isLnInvoice()
    return isPrefixCorrect && isPathCorrect
}

fun String.isBitcoinUri() =
    startsWith(prefix = "bitcoin:", ignoreCase = true) &&
        this.split(":").lastOrNull()?.split("?")?.firstOrNull().isBitcoinAddress()

fun String?.isBitcoinAddress(): Boolean {
    if (this == null) return false

    val result = runCatching {
        Address.fromString(MainNetParams.get(), this)
    }
    return result.getOrNull() != null
}

fun String.parseLightningPaymentInstructions(): String? {
    return when {
        isLightningUri() -> this.split(":").last()
        else -> null
    }
}

fun String.parseBitcoinPaymentInstructions(): BitcoinPaymentInstruction? {
    return when {
        isBitcoinAddress() -> BitcoinPaymentInstruction(address = this)

        isBitcoinUri() -> {
            val path = this.split(":").lastOrNull()
            if (path?.isBitcoinAddress() == true) {
                BitcoinPaymentInstruction(address = path)
            } else {
                val chunks = path?.split("?")
                val address = chunks?.firstOrNull()
                val queryString = chunks?.lastOrNull()

                if (address?.isBitcoinAddress() == true) {
                    val params = queryString?.split("&")

                    val amountParam = params?.find { it.startsWith("amount") }
                    val amount = amountParam?.split("=")?.lastOrNull()

                    val labelParam = params?.find { it.startsWith("label") }
                    val label = labelParam?.split("=")?.lastOrNull()?.asUrlDecoded()

                    val lightningParam = params?.find { it.startsWith("lightning") }
                    val lightning = lightningParam?.split("=")?.lastOrNull()

                    BitcoinPaymentInstruction(
                        address = address,
                        lightning = lightning,
                        amount = amount,
                        label = label,
                    )
                } else {
                    null
                }
            }
        }

        else -> null
    }
}

fun String.parseSatsToUsd(currentExchangeRate: Double?): String {
    return this.toBigDecimal()
        .fromSatsToUsd(currentExchangeRate)
        .toPlainString()
}

fun String.parseUsdToSats(currentExchangeRate: Double?): String {
    return this.toBigDecimal()
        .fromUsdToSats(currentExchangeRate)
        .toString()
}
