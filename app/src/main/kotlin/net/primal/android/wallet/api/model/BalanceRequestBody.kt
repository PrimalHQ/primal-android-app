package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceRequestBody(
    @SerialName("subwallet") val subWallet: Int = 1,
) : WalletOperationRequestBody()
