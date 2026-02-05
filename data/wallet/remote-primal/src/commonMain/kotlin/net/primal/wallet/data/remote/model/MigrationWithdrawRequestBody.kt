package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationWithdrawRequestBody(
    @SerialName("lnInvoice") val lnInvoice: String,
) : WalletOperationRequestBody()
