package net.primal.android.settings.wallet.domain

data class NwcConnectionInfo(
    val nwcPubkey: String,
    val appName: String,
    val dailyBudget: String?,
    val canRevoke: Boolean = true,
)
