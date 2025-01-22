package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimalNwcConnectionInfo(
    @SerialName("appname") val appName: String,
)
