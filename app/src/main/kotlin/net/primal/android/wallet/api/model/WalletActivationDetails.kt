package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletActivationDetails(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    @SerialName("country") val country: String,
    @SerialName("state") val state: String,
)
