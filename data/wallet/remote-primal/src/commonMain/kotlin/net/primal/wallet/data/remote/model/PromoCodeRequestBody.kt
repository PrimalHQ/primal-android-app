package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromoCodeRequestBody(
    @SerialName("promo_code") val promoCode: String,
) : WalletOperationRequestBody()
