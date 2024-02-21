package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetActivationCodeRequestBody(
    @SerialName("user_details") val userDetails: WalletActivationDetails,
) : WalletOperationRequestBody()
