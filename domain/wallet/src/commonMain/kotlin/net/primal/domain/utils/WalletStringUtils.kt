@file:Suppress("TooManyFunctions")

package net.primal.domain.utils

import fr.acinq.bitcoin.Base58
import fr.acinq.bitcoin.Bech32
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.CurrencyConversionUtils.fromUsdToSats
import net.primal.core.utils.CurrencyConversionUtils.toBigDecimal
import net.primal.core.utils.asUrlDecoded
import net.primal.core.utils.isEmailAddress
import net.primal.domain.wallet.BitcoinPaymentInstruction

fun String.isLnInvoice() = startsWith(prefix = "lnbc", ignoreCase = true)

fun String.isLnUrl() = startsWith(prefix = "lnurl", ignoreCase = true)

fun String.isLightningAddress() = isEmailAddress()

fun String.isLightningUri(): Boolean {
    val isPrefixCorrect = startsWith(prefix = "lightning:", ignoreCase = true)
    val path = this.split(":").last()
    val isPathCorrect = path.isLightningAddress() || path.isLnUrl() || path.isLnInvoice()
    return isPrefixCorrect && isPathCorrect
}

fun String.isBitcoinUri() =
    startsWith(prefix = "bitcoin:", ignoreCase = true) &&
        this.split(":").lastOrNull()?.split("?")?.firstOrNull().isBitcoinAddress()

/**
 * Returns `true` when the receiver is _syntactically_ a valid Bitcoin address
 * (legacy Base58Check **or** SegWit Bech32/Bech32m).
 */
fun String?.isBitcoinAddress(): Boolean =
    when {
        this == null -> false
        // Legacy P2PKH / P2SH (Base58Check).
        startsWith("1") || startsWith("3") ->
            runCatching { Base58.decode(this) }.isSuccess
        // SegWit v0-16 and Taproot (Bech32 / Bech32m).
        startsWith("bc1", ignoreCase = true) ||
            startsWith("tb1", ignoreCase = true) ||
            startsWith("bcrt1", ignoreCase = true) ->
            runCatching { Bech32.decode(this.lowercase()) }.isSuccess

        else -> false
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
