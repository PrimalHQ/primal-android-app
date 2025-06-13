package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.wallet.SubWallet

@Serializable
data class BalanceRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
) : WalletOperationRequestBody()
