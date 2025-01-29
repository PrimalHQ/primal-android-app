package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LegendLeaderboardRequest(
    @SerialName("order_by") val orderBy: LeaderboardOrderBy,
    val limit: Int = 1000,
)

@Serializable
enum class LeaderboardOrderBy {
    @SerialName("donated_btc")
    DonatedBtc,
    @SerialName("last_donation")
    LastDonation,
}
