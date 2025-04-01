package net.primal.android.premium.leaderboard.legend.ui.model

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.CdnImage
import net.primal.domain.LeaderboardLegendEntry

data class LeaderboardUiLegend(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val legendSince: Long?,
    val legendaryCustomization: LegendaryCustomization?,
    val donatedSats: ULong,
)

fun LeaderboardLegendEntry.toUiModel() =
    LeaderboardUiLegend(
        userId = userId,
        avatarCdnImage = avatarCdnImage,
        displayName = displayName,
        internetIdentifier = internetIdentifier,
        legendSince = legendSince,
        legendaryCustomization = primalLegendProfile?.asLegendaryCustomization(),
        donatedSats = donatedSats,
    )
