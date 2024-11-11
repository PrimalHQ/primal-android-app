package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowSupportUsResponse(
    @SerialName("show_primal_support") val showSupportPrimal: Boolean = false,
)
