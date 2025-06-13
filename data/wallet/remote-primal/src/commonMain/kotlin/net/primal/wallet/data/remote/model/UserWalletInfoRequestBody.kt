package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserWalletInfoRequestBody(
    @SerialName("pubkey") val userId: String,
) : WalletOperationRequestBody()
