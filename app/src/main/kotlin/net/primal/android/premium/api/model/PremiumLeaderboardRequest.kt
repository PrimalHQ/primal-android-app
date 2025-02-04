package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PremiumLeaderboardRequest(
    @SerialName("order_by") val orderBy: PremiumLeaderboardOrderBy,
    val limit: Int = 100,
)

enum class PremiumLeaderboardOrderBy {
    @SerialName("index")
    Index,
}
