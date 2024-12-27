package net.primal.android.wallet.transactions.receive.model

data class PaymentDetails(
    val amountInBtc: String? = null,
    val amountInUsd: String? = null,
    val comment: String? = null,
)
