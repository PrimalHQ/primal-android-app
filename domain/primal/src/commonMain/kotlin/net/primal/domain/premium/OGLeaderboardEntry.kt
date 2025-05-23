package net.primal.domain.premium

import net.primal.domain.links.CdnImage

data class OGLeaderboardEntry(
    val index: Int,
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val firstCohort: String?,
    val secondCohort: String?,
    val premiumSince: Long?,
)
