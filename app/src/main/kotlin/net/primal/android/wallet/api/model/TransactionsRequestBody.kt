package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.wallet.domain.SubWallet

@Serializable
data class TransactionsRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since_updated_at") val since: Long? = null,
    @SerialName("min_amount_btc") val minAmountInBtc: String? = null,
) : WalletOperationRequestBody()
