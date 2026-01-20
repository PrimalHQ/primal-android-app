package net.primal.tsunami.model

data class LightningReceiveRequest(
    val status: LightningRequestStatus,
    val encodedInvoice: String,
)
