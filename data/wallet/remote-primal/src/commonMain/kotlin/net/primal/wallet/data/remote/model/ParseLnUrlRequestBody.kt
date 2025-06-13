package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseLnUrlRequestBody(
    @SerialName("target_lnurl") val lnurl: String,
) : WalletOperationRequestBody()
