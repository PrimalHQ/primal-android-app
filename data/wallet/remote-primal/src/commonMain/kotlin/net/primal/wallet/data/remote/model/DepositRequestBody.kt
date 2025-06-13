package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.wallet.Network
import net.primal.domain.wallet.SubWallet

@Serializable
data class DepositRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
    @SerialName("amount_btc") val amountBtc: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("network") val network: Network? = null,
) : WalletOperationRequestBody()
