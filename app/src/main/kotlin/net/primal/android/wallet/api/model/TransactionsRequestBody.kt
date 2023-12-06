package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionsRequestBody(
    @SerialName("subwallet") val subWallet: Int = 1,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
) : WalletOperationRequestBody()
