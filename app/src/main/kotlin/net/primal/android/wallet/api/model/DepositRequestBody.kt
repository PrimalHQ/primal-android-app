package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.domain.SubWallet

@Serializable
data class DepositRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
    @SerialName("amount_btc") val amountBtc: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("network") val network: Network? = null,
) : WalletOperationRequestBody()
