package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NwcRevokeConnectionRequestBody(
    @SerialName("nwc_pubkey") val nwcPubKey: String,
) : WalletOperationRequestBody()
