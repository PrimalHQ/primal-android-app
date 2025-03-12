package net.primal.android.premium.leaderboard.domain

import net.primal.android.events.domain.CdnImage
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi

data class LeaderboardLegendEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val premiumProfileDataUi: PremiumProfileDataUi?,
    val donatedSats: ULong,
)
