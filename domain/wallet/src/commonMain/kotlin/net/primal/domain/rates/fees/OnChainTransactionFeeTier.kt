package net.primal.domain.rates.fees

data class OnChainTransactionFeeTier(
    val tierId: String,
    val txFeeInBtc: String,
    val label: String? = null,
    val confirmationEstimationInMin: Int? = null,
    val minAmountInBtc: String? = null,
    val expiresAt: Long? = null,
)
