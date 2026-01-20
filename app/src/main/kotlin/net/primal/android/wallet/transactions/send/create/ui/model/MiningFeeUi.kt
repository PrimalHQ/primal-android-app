package net.primal.android.wallet.transactions.send.create.ui.model

data class MiningFeeUi(
    val id: String,
    val feeInBtc: String,
    val label: String? = null,
    val confirmationEstimateInMin: Int? = null,
    val minAmountInBtc: String? = null,
)
