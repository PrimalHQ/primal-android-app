package net.primal.android.settings.wallet.model

data class ConnectedAppUi(
    val nwcPubkey: String,
    val appName: String,
    val dailyBudget: String,
    val canRevoke: Boolean = true,
)
