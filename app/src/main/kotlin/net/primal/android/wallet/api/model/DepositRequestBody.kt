package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DepositRequestBody(
    @SerialName("subwallet") val subWallet: Int = 1,
    @SerialName("amount_btc") val amountBtc: String? = null,
    @SerialName("description") val description: String? = null,
) : WalletOperationRequestBody()
