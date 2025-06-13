package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NwcRevokeConnectionRequestBody(
    @SerialName("nwc_pubkey") val nwcPubKey: String,
) : WalletOperationRequestBody()
