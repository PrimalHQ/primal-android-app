package net.primal.android.wallet.utils

data class BitcoinPaymentInstruction(
    val address: String,
    val amount: String? = null,
    val label: String? = null,
) {
    fun hasParams() = amount != null || label != null
}
