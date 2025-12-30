package net.primal.data.account.signer.local.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("Result")
@Serializable
data class SignerIntentResult(
    @SerialName("package") val packageName: String,
    val result: String,
    val id: String,
)
