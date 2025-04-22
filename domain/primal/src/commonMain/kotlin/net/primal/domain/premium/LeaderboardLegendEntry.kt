package net.primal.domain.premium

import net.primal.domain.links.CdnImage

data class LeaderboardLegendEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val legendSince: Long?,
    val primalLegendProfile: PrimalLegendProfile?,
    val donatedSats: ULong,
)
