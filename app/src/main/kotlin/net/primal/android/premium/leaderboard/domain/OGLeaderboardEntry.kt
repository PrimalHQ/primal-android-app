package net.primal.android.premium.leaderboard.domain

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership

data class OGLeaderboardEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val premiumMembership: PremiumMembership?,
)
