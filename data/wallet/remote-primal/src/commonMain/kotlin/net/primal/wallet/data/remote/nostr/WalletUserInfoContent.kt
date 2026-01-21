package net.primal.wallet.data.remote.nostr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletUserInfoContent(
    @SerialName("kyc_level") val kycLevel: Int,
    val lud16: String,
)
