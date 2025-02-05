package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PremiumLeaderboardRequest(
    @SerialName("order_by") val orderBy: PremiumLeaderboardOrderBy,
    val limit: Int,
    val since: Long?,
    val until: Long?,
)

enum class PremiumLeaderboardOrderBy {
    @SerialName("premium_since")
    PremiumSince,

    @SerialName("index")
    Index,
}
