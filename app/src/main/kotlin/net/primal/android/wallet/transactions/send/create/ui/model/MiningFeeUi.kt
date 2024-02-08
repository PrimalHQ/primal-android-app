package net.primal.android.wallet.transactions.send.create.ui.model

data class MiningFeeUi(
    val id: String,
    val label: String,
    val confirmationEstimateInMin: Int,
    val feeInBtc: String,
    val minAmountInBtc: String? = null,
)
