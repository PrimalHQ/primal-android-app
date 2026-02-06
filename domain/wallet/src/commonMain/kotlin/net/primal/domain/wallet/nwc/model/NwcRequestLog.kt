package net.primal.domain.wallet.nwc.model

data class NwcRequestLog(
    val eventId: String,
    val connectionId: String,
    val walletId: String,
    val userId: String,
    val method: String,
    val requestPayload: String,
    val responsePayload: String?,
    val requestState: NwcRequestState,
    val errorCode: String?,
    val errorMessage: String?,
    val requestedAt: Long,
    val completedAt: Long?,
    val amountMsats: Long?,
)
