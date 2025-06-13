package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateWalletRequestBody(
    @SerialName("activation_code") val code: String,
) : WalletOperationRequestBody()
