package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateWalletRequestBody(
    @SerialName("activation_code") val code: String,
) : WalletOperationRequestBody()
