package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletStatusResponse(
    @SerialName("has_custodial_wallet") val hasCustodialWallet: Boolean,
    @SerialName("has_spark_wallet") val hasSparkWallet: Boolean,
)
