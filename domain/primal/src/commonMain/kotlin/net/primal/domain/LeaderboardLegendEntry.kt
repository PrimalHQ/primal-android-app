package net.primal.domain

data class LeaderboardLegendEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val legendSince: Long?,
    val primalLegendProfile: PrimalLegendProfile?,
    val donatedSats: ULong,
)
