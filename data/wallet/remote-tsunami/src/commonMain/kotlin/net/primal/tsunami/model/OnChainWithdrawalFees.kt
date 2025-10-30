package net.primal.tsunami.model

data class OnChainWithdrawalFees(
    val lowPriorityInSats: ULong,
    val mediumPriorityInSats: ULong,
    val highPriorityInSats: ULong,
)
