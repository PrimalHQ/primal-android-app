package net.primal.android.wallet.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GetActivationCodeRequestBody(
    val name: String,
    val email: String,
) : WalletOperationRequestBody()
