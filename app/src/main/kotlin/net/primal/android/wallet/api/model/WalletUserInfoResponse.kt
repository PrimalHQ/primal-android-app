package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletUserInfoResponse(
    @SerialName("kyc_level") val kycLevel: Int,
    @SerialName("lud16") val lightningAddress: String,
)
