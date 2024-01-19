package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.SubWallet

@Serializable
data class TransactionsRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
    @SerialName("min_amount_btc") val minAmountInBtc: String? = null,
) : WalletOperationRequestBody()
