package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipPurchaseMonitorRequestBody(
    @SerialName("membership_quote_id") val membershipQuoteId: String,
)
