package net.primal.android.premium.leaderboard.domain

import net.primal.android.events.domain.CdnImage

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
