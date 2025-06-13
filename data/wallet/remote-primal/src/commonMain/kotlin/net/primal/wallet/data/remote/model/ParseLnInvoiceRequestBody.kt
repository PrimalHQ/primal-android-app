package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseLnInvoiceRequestBody(
    @SerialName("lninvoice") val lnbc: String,
) : WalletOperationRequestBody()
