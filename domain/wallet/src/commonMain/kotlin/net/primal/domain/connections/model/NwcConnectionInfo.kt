package net.primal.domain.connections.model

data class NwcConnectionInfo(
    val appName: String,
    val dailyBudgetInBtc: String?,
    val nwcPubkey: String,
)
