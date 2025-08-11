package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MiningFeesRequestBody(
    @SerialName("target_bcaddr") val btcAddress: String,
    @SerialName("amount_btc") val amountInBtc: String,
) : WalletOperationRequestBody()
