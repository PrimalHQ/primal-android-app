package net.primal.tsunami.model

data class LightningSendRequest(
    val status: LightningRequestStatus,
    val encodedInvoice: String,
    val feeInMillisats: Long,
)
