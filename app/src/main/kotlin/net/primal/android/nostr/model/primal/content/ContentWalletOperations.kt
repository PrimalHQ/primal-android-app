package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletUserInfoContent(
    @SerialName("kyc_level") val kycLevel: Int,
    val lud16: String,
)

@Serializable
data class WalletActivationContent(
    val lud16: String,
)
