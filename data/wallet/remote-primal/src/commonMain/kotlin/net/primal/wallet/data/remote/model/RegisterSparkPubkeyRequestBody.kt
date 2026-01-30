package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterSparkPubkeyRequestBody(
    @SerialName("spark_pubkey") val sparkPubkey: String,
) : WalletOperationRequestBody()
