package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimalNwcConnectionInfo(
    @SerialName("appname") val appName: String,
    @SerialName("daily_budget_btc") val dailyBudget: String?,
    @SerialName("nwc_pubkey") val nwcPubkey: String,
)
