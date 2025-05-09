package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromoCodeDetailsResponse(
    @SerialName("welcome_message") val welcomeMessage: String?,
    @SerialName("preloaded_btc") val preloadedBtc: Double?,
    @SerialName("origin_pubkey") val originPubkey: String?,
)
