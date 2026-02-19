package net.primal.domain.wallet

data class PayResult(
    val preimage: String? = null,
    val feesPaid: Long? = null,
)
