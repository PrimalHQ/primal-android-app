package net.primal.domain.rates.fees

data class OnChainTransactionFeeTier(
    val tierId: String,
    val label: String,
    val confirmationEstimationInMin: Int,
    val txFeeInBtc: String,
    val minAmountInBtc: String? = null,
)
