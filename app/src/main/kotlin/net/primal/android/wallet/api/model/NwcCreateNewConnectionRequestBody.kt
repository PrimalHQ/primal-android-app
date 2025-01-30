package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NwcCreateNewConnectionRequestBody(
    @SerialName("appname") val appName: String,
    @SerialName("daily_budget_btc") val dailyBudgetBtc: String?,
) : WalletOperationRequestBody()
