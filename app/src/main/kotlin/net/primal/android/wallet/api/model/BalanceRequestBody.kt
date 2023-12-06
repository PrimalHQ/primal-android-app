package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.SubWallet

@Serializable
data class BalanceRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
) : WalletOperationRequestBody()
