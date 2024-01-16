package net.primal.android.wallet.transactions.receive

data class PaymentDetails(
    val amountInBtc: String? = null,
    val lightningAddress: String? = null,
    val comment: String? = null,
    val invoice: String? = null,
)
