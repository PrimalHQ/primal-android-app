package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewNwcConnectionInfo(
    @SerialName("nwc_pubkey") val nwcPubkey: String,
    @SerialName("uri") val nwcConnectionUri: String,
)
