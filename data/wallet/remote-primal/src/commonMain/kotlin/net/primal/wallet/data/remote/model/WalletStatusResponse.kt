package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletStatusResponse(
    @SerialName("has_spark_wallet") val hasSparkWallet: Boolean,
    @SerialName("lightning_network_address") val lightningAddress: String? = null,
    @SerialName("spark_pubkey") val sparkPubkey: String? = null,
)
