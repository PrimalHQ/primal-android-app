package net.primal.android.settings.wallet.model

data class ConnectedAppUi(
    val id: String,
    val appName: String,
    val dailyBudget: String,
    val canRevoke: Boolean = true,
)
