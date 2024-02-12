package net.primal.android.wallet.utils

import android.util.Patterns
import java.util.regex.Pattern
import net.primal.android.navigation.asUrlDecoded

fun String.isLnInvoice() = startsWith(prefix = "lnbc", ignoreCase = true)

fun String.isLnUrl() = startsWith(prefix = "lnurl", ignoreCase = true)

fun String.isLightningAddress() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isLightningAddressUri(): Boolean {
    val isPrefixCorrect = startsWith(prefix = "lightning:", ignoreCase = true)
    val path = this.split(":").last()
    val isPathCorrect = path.isLightningAddress() || path.isLnUrl()
    return isPrefixCorrect && isPathCorrect
}

fun String.isBitcoinAddressUri() =
    startsWith(prefix = "bitcoin:", ignoreCase = true) &&
        this.split(":").lastOrNull()?.split("?")?.firstOrNull().isBitcoinAddress()

private val btcAddressPattern = Pattern.compile(
    "^(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,39}\$",
    Pattern.CASE_INSENSITIVE,
)

fun String?.isBitcoinAddress(): Boolean {
    return when (this) {
        null -> false
        else -> btcAddressPattern.matcher(this).matches()
    }
}

fun String.parseBitcoinPaymentInstructions(): BitcoinPaymentInstruction? {
    return when {
        isBitcoinAddress() -> BitcoinPaymentInstruction(address = this)

        isBitcoinAddressUri() -> {
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

                    BitcoinPaymentInstruction(
                        address = address,
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
