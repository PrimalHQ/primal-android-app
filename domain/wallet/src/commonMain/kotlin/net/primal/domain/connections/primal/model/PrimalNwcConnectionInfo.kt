package net.primal.domain.connections.primal.model

data class PrimalNwcConnectionInfo(
    val appName: String,
    val dailyBudgetInBtc: String?,
    val nwcPubkey: String,
)
